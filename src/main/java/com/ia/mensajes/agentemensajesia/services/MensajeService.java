package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.dao.MensajeDAO;
import com.ia.mensajes.agentemensajesia.ia.ClasificadorMensajes;
import com.ia.mensajes.agentemensajesia.ia.ResultadoClasificacion;
import com.ia.mensajes.agentemensajesia.model.EstadisticaMensaje;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.model.PaginatedResponse;
import com.ia.mensajes.agentemensajesia.resources.MensajeResource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MensajeService {

    private final MensajeDAO mensajeDAO = new MensajeDAO();

    public void procesarYGuardarMensajesDesdeExcel(InputStream inputStream, String loteId) throws Exception {
        
        ClasificadorMensajes.getInstance().waitForReady();

        mensajeDAO.borrarTodos();
        
        List<Row> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.rowIterator().forEachRemaining(rows::add);
            if (!rows.isEmpty()) rows.remove(0);
        }

        if (rows.isEmpty()) return;

        final long totalRows = rows.size();
        final AtomicLong processedCount = new AtomicLong(0);
        final DataFormatter formatter = new DataFormatter();

        MensajeResource.jobStatuses.get(loteId).setStatus("PROCESANDO");

        List<Mensaje> mensajesProcesados = rows.parallelStream()
            .map(row -> {
                String textoMensaje = getCellValueAsString(row.getCell(7), formatter);
                if (textoMensaje == null || textoMensaje.isEmpty()) return null;

                long count = processedCount.incrementAndGet();
                int progress = (int) (((double) count / totalRows) * 100);
                MensajeResource.jobStatuses.get(loteId).setProgress(progress);

                ClasificadorMensajes clasificador = ClasificadorMensajes.getInstance();
                ResultadoClasificacion resultado = clasificador.clasificar(textoMensaje);

                Mensaje nuevoMensaje = new Mensaje();
                nuevoMensaje.setAplicacion(getCellValueAsString(row.getCell(0), formatter));
                nuevoMensaje.setIdCliente(getCellValueAsString(row.getCell(1), formatter));
                nuevoMensaje.setTexto(textoMensaje);
                nuevoMensaje.setNombreAsesor(getCellValueAsString(row.getCell(9), formatter));
                nuevoMensaje.setFechaHoraMensaje(getCellLocalDateTime(row.getCell(10), formatter));
                nuevoMensaje.setClasificacion(resultado.getCategoria());
                nuevoMensaje.setObservacion(resultado.getObservacion());
                nuevoMensaje.setFechaProcesamiento(new Date());
                nuevoMensaje.setLote(loteId);

                return nuevoMensaje;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (!mensajesProcesados.isEmpty()) {
            mensajeDAO.guardarVarios(mensajesProcesados);
        }
    }

    private String getCellValueAsString(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        return formatter.formatCellValue(cell).trim();
    }
    
    private LocalDateTime getCellLocalDateTime(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo parsear la fecha de la celda " + cell.getAddress() + ". Valor: " + getCellValueAsString(cell, formatter));
        }
        return null;
    }

    public PaginatedResponse<Mensaje> obtenerMensajesPaginado(int numeroPagina, int tamanoPagina) {
        long totalMensajes = mensajeDAO.contarTotalMensajes();
        if (totalMensajes == 0) {
            return new PaginatedResponse<>(Collections.emptyList(), 0, 0, 0);
        }
        List<Mensaje> mensajes = mensajeDAO.buscarPaginado(numeroPagina, tamanoPagina);
        int totalPaginas = (int) Math.ceil((double) totalMensajes / tamanoPagina);
        return new PaginatedResponse<>(mensajes, numeroPagina, totalPaginas, totalMensajes);
    }

    public PaginatedResponse<Mensaje> obtenerMensajesPaginadoPorLote(String loteId, int numeroPagina, int tamanoPagina) {
        long totalMensajes = mensajeDAO.contarTotalMensajesPorLote(loteId);
        if (totalMensajes == 0) {
            return new PaginatedResponse<>(Collections.emptyList(), 0, 0, 0);
        }
        List<Mensaje> mensajes = mensajeDAO.buscarPaginadoPorLote(loteId, numeroPagina, tamanoPagina);
        int totalPaginas = (int) Math.ceil((double) totalMensajes / tamanoPagina);
        return new PaginatedResponse<>(mensajes, numeroPagina, totalPaginas, totalMensajes);
    }

    public List<Mensaje> obtenerTodosLosMensajes() {
        return mensajeDAO.buscarTodos();
    }

    public EstadisticaMensaje calcularEstadisticas() {
        return mensajeDAO.getEstadisticas();
    }
}