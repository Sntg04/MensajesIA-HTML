package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.MensajeDAO;
import com.ia.mensajes.agentemensajesia.ia.ClasificadorMensajes;
import com.ia.mensajes.agentemensajesia.ia.ResultadoClasificacion;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MensajeService {

    private final MensajeDAO mensajeDAO = new MensajeDAO();
    // Se elimina la instancia única del clasificador de aquí.

    public List<Mensaje> procesarArchivoExcel(InputStream inputStream) throws IOException {
        List<Mensaje> mensajesDelArchivo = new ArrayList<>();
        String loteCargaId = UUID.randomUUID().toString();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet primeraHoja = workbook.getSheetAt(0);
            Iterator<Row> iterator = primeraHoja.iterator();
            if (iterator.hasNext()) {
                iterator.next(); // Omitir la cabecera
            }

            while (iterator.hasNext()) {
                Row siguienteFila = iterator.next();
                
                String aplicacion = getStringValueFromCell(siguienteFila.getCell(0));
                String textoOriginal = getStringValueFromCell(siguienteFila.getCell(7));
                String nombreAsesor = getStringValueFromCell(siguienteFila.getCell(9));
                Cell celdaFechaHora = siguienteFila.getCell(10);

                if (nombreAsesor == null || nombreAsesor.trim().isEmpty() || textoOriginal == null || textoOriginal.trim().isEmpty()) {
                    continue; 
                }

                Mensaje mensaje = new Mensaje();
                mensaje.setAplicacion(aplicacion != null ? aplicacion : "N/A");
                mensaje.setNombreAsesor(nombreAsesor);
                mensaje.setTextoOriginal(textoOriginal);
                mensaje.setLoteCarga(loteCargaId);
                
                if (celdaFechaHora != null && celdaFechaHora.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(celdaFechaHora)) {
                    Date fechaHoraCompleta = celdaFechaHora.getDateCellValue();
                    LocalDateTime ldt = fechaHoraCompleta.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    mensaje.setFechaMensaje(java.sql.Date.valueOf(ldt.toLocalDate()));
                    mensaje.setHoraMensaje(ldt.toLocalTime());
                }
                
                mensajesDelArchivo.add(mensaje);
            }
        }

        // --- CAMBIO CLAVE: Procesamiento en paralelo ---
        List<Mensaje> mensajesProcesados = Collections.synchronizedList(new ArrayList<>());
        
        mensajesDelArchivo.parallelStream().forEach(mensaje -> {
            ClasificadorMensajes clasificador = ClasificadorMensajes.getInstance(); // Obtiene la instancia segura para el hilo
            ResultadoClasificacion resultado = clasificador.clasificar(mensaje.getTextoOriginal());
            mensaje.setClasificacion(resultado.getClasificacion());

            if ("Alerta".equals(resultado.getClasificacion())) {
                mensaje.setNecesitaRevision(true);
                String motivo = "Motivo: " + resultado.getPalabraClaveEncontrada() + ". ";
                mensaje.setTextoReescrito(motivo + clasificador.reescribir(mensaje.getTextoOriginal()));
            } else {
                mensaje.setNecesitaRevision(false);
                mensaje.setTextoReescrito("");
            }
            
            mensaje.setConteoPalabras(mensaje.getTextoOriginal().split("\\s+").length);
            mensaje.setConteoCaracteres(mensaje.getTextoOriginal().length());
            
            mensajesProcesados.add(mensaje);
        });


        if (!mensajesProcesados.isEmpty()) {
            mensajeDAO.guardarVarios(mensajesProcesados);
        }
        return mensajesProcesados;
    }

    private String getStringValueFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    public List<Mensaje> obtenerAlertasPorLote(String loteId) {
        if (loteId == null || loteId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return mensajeDAO.listarAlertasPorLote(loteId);
    }

    public ByteArrayInputStream exportarAlertasAExcel(String loteId) throws IOException {
        List<Mensaje> alertas = obtenerAlertasPorLote(loteId);
        return ExcelExportService.crearReporteCompletoDeLote(alertas);
    }

    public ByteArrayInputStream exportarLoteCompletoAExcel(String loteId) throws IOException {
        List<Mensaje> mensajesDelLote = mensajeDAO.listarPorLote(loteId);
        return ExcelExportService.crearReporteCompletoDeLote(mensajesDelLote);
    }
}