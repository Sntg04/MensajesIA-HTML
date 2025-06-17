package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.model.Mensaje;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelExportService {

    /**
     * Crea un reporte de Excel con 3 hojas: Todos los mensajes, solo los buenos, y solo las alertas.
     * @param mensajes La lista completa de mensajes de un lote.
     * @return Un ByteArrayInputStream que representa el archivo Excel.
     * @throws IOException Si ocurre un error al escribir el libro de trabajo.
     */
    public static ByteArrayInputStream crearReporteCompletoDeLote(List<Mensaje> mensajes) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Filtrar las listas para las hojas específicas
            List<Mensaje> buenos = mensajes.stream()
                .filter(m -> "Bueno".equals(m.getClasificacion()))
                .collect(Collectors.toList());
            List<Mensaje> alertas = mensajes.stream()
                .filter(m -> "Alerta".equals(m.getClasificacion()))
                .collect(Collectors.toList());

            // Crear Hoja 1: Todos los mensajes
            crearHojaDeReporte(workbook, "Todos_los_Mensajes", mensajes);
            
            // Crear Hoja 2: Mensajes Buenos
            crearHojaDeReporte(workbook, "Mensajes_Buenos", buenos);

            // Crear Hoja 3: Mensajes de Alerta
            crearHojaDeReporte(workbook, "Mensajes_Alerta", alertas);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Función de utilidad reutilizable para crear una hoja de Excel y llenarla con datos de mensajes.
     * @param workbook El libro de trabajo de Excel (XSSFWorkbook).
     * @param nombreHoja El nombre que se le dará a la nueva hoja.
     * @param listaMensajes La lista de mensajes para escribir en la hoja.
     */
    private static void crearHojaDeReporte(XSSFWorkbook workbook, String nombreHoja, List<Mensaje> listaMensajes) {
        XSSFSheet sheet = workbook.createSheet(nombreHoja);
        Row headerRow = sheet.createRow(0);
        
        // Definir las cabeceras de las columnas
        String[] headers = {"ID", "Asesor", "Aplicación", "Mensaje Original", "Clasificación", "Sugerencia", "Fecha", "Hora"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowIdx = 1;
        for (Mensaje msg : listaMensajes) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(msg.getId() != null ? msg.getId() : 0);
            row.createCell(1).setCellValue(msg.getNombreAsesor());
            row.createCell(2).setCellValue(msg.getAplicacion());
            row.createCell(3).setCellValue(msg.getTextoOriginal());
            row.createCell(4).setCellValue(msg.getClasificacion());
            row.createCell(5).setCellValue(msg.getTextoReescrito());
            row.createCell(6).setCellValue(msg.getFechaMensaje() != null ? msg.getFechaMensaje().toString() : "N/A");
            row.createCell(7).setCellValue(msg.getHoraMensaje() != null ? msg.getHoraMensaje().toString() : "N/A");
        }
    }
}