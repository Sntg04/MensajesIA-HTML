package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.MensajeDAO;
import com.ia.mensajes.agentemensajesia.ia.ClasificadorMensajes;
import com.ia.mensajes.agentemensajesia.ia.ResultadoClasificacion;
import com.ia.mensajes.agentemensajesia.model.EstadisticaMensaje;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MensajeService {

    private final MensajeDAO mensajeDAO = new MensajeDAO();
    private final ClasificadorMensajes clasificador;

    public MensajeService() {
        this.clasificador = new ClasificadorMensajes();
        this.clasificador.cargarModelos();
    }

    public List<Mensaje> procesarYGuardarMensajesDesdeExcel(InputStream inputStream) throws Exception {
        List<Mensaje> mensajesAGuardar = new ArrayList<>();
        // Crear un identificador único para este lote de carga
        String loteId = UUID.randomUUID().toString();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Omitir encabezado
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Cell cell = row.getCell(0);
                if (cell != null) {
                    String textoMensaje = cell.getStringCellValue();
                    if (textoMensaje != null && !textoMensaje.trim().isEmpty()) {
                        ResultadoClasificacion resultado = clasificador.clasificar(textoMensaje);
                        Mensaje nuevoMensaje = new Mensaje();
                        nuevoMensaje.setTexto(textoMensaje);
                        nuevoMensaje.setClasificacion(resultado.getCategoria());
                        nuevoMensaje.setConfianza(resultado.getConfianza());
                        nuevoMensaje.setFechaProcesamiento(new Date());
                        nuevoMensaje.setLote(loteId); // Asignar el ID del lote
                        mensajesAGuardar.add(nuevoMensaje);
                    }
                }
            }
        }
        
        // --- ¡CORRECCIÓN CLAVE! ---
        // Guardar todos los mensajes en una sola transacción
        if (!mensajesAGuardar.isEmpty()) {
            mensajeDAO.guardarVarios(mensajesAGuardar);
        }
        
        return mensajesAGuardar;
    }

    public List<Mensaje> obtenerTodosLosMensajes() {
        return mensajeDAO.buscarTodos();
    }

    public EstadisticaMensaje calcularEstadisticas() {
        return mensajeDAO.getEstadisticas();
    }
    
    // --- MÉTODOS QUE AHORA FUNCIONARÁN GRACIAS A LOS CAMBIOS EN EL DAO ---
    public List<Mensaje> obtenerAlertasPorLote(String loteId) {
        return mensajeDAO.listarAlertasPorLote(loteId);
    }
    
    public List<Mensaje> obtenerMensajesPorLote(String loteId) {
        return mensajeDAO.listarPorLote(loteId);
    }
}