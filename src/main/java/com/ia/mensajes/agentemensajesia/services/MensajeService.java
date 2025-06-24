package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.MensajeDAO;
import com.ia.mensajes.agentemensajesia.ia.ClasificadorMensajes;
import com.ia.mensajes.agentemensajesia.ia.ResultadoClasificacion;
import com.ia.mensajes.agentemensajesia.model.EstadisticaMensaje;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MensajeService {

    private final MensajeDAO mensajeDAO = new MensajeDAO();

    public List<Mensaje> procesarYGuardarMensajesDesdeExcel(InputStream inputStream) throws Exception {
        List<Mensaje> mensajesAGuardar = new ArrayList<>();
        String loteId = UUID.randomUUID().toString();
        ClasificadorMensajes clasificador = ClasificadorMensajes.getInstance();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) rowIterator.next(); // Omitir encabezado

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                String app = getCellValueAsString(row.getCell(0)); // Columna A
                String idCliente = getCellValueAsString(row.getCell(1)); // Columna B
                String textoMensaje = getCellValueAsString(row.getCell(7)); // Columna H
                String asesor = getCellValueAsString(row.getCell(9)); // Columna J
                LocalDateTime fechaHora = getCellLocalDateTime(row.getCell(10)); // Columna K

                if (textoMensaje != null && !textoMensaje.isEmpty()) {
                    ResultadoClasificacion resultado = clasificador.clasificar(textoMensaje);

                    Mensaje nuevoMensaje = new Mensaje();
                    nuevoMensaje.setAplicacion(app);
                    nuevoMensaje.setIdCliente(idCliente);
                    nuevoMensaje.setTexto(textoMensaje);
                    nuevoMensaje.setNombreAsesor(asesor);
                    nuevoMensaje.setFechaHoraMensaje(fechaHora);
                    
                    nuevoMensaje.setClasificacion(resultado.getCategoria());
                    nuevoMensaje.setObservacion(resultado.getObservacion());
                    
                    nuevoMensaje.setFechaProcesamiento(new Date());
                    nuevoMensaje.setLote(loteId);
                    
                    mensajesAGuardar.add(nuevoMensaje);
                }
            }
        }

        if (!mensajesAGuardar.isEmpty()) {
            mensajeDAO.guardarVarios(mensajesAGuardar);
        }
        return mensajesAGuardar;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }
    
    private LocalDateTime getCellLocalDateTime(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
        } catch (Exception e) {
            System.err.println("No se pudo parsear la fecha de la celda: " + cell.getAddress());
            return null;
        }
        return null;
    }

    public List<Mensaje> obtenerTodosLosMensajes() { return mensajeDAO.buscarTodos(); }
    public EstadisticaMensaje calcularEstadisticas() { return mensajeDAO.getEstadisticas(); }
}