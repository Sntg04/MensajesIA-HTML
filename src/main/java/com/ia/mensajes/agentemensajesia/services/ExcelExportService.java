package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.model.Mensaje;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelExportService {

    /**
     * Método principal que crea un libro de Excel con 3 hojas: Todos, Buenos y Alertas.
     * @param mensajes La lista completa de mensajes a procesar.
     * @return Un ByteArrayInputStream del archivo Excel generado.
     * @throws IOException Si ocurre un error al escribir el libro.
     */
    public ByteArrayInputStream exportarMensajesAExcel(List<Mensaje> mensajes) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // 1. Filtrar las listas de mensajes
            List<Mensaje> mensajesBuenos = mensajes.stream()
                    .filter(m -> "Bueno".equals(m.getClasificacion()))
                    .collect(Collectors.toList());

            List<Mensaje> mensajesAlerta = mensajes.stream()
                    .filter(m -> "Alerta".equals(m.getClasificacion()))
                    .collect(Collectors.toList());

            // 2. Crear cada una de las hojas llamando a un método de ayuda
            crearHojaDeReporte(workbook, "Todos los Mensajes", mensajes);
            crearHojaDeReporte(workbook, "Mensajes Buenos", mensajesBuenos);
            crearHojaDeReporte(workbook, "Mensajes Alerta", mensajesAlerta);

            // 3. Escribir el libro completo en el stream de salida
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * Método de ayuda privado para crear una hoja de Excel con una lista de mensajes.
     * @param workbook El libro de Excel en el que se creará la hoja.
     * @param nombreHoja El nombre que se le dará a la nueva hoja.
     * @param listaMensajes La lista de mensajes a escribir en la hoja.
     */
    private void crearHojaDeReporte(Workbook workbook, String nombreHoja, List<Mensaje> listaMensajes) {
        String[] columns = {"ID", "Asesor", "Aplicación", "ID Cliente", "Fecha Mensaje", "Mensaje", "Clasificación", "Observación"};

        Sheet sheet = workbook.createSheet(nombreHoja);

        // Estilo para los encabezados
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Crear la fila de encabezados
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Formateador para la fecha
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        // Llenar las filas con los datos de los mensajes
        int rowIdx = 1;
        for (Mensaje mensaje : listaMensajes) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(mensaje.getId());
            row.createCell(1).setCellValue(mensaje.getNombreAsesor());
            row.createCell(2).setCellValue(mensaje.getAplicacion());
            row.createCell(3).setCellValue(mensaje.getIdCliente());

            String fechaFormateada = mensaje.getFechaHoraMensaje() != null ? mensaje.getFechaHoraMensaje().format(formatter) : "N/A";
            row.createCell(4).setCellValue(fechaFormateada);

            row.createCell(5).setCellValue(mensaje.getTexto());
            row.createCell(6).setCellValue(mensaje.getClasificacion());
            row.createCell(7).setCellValue(mensaje.getObservacion());
        }

        // Ajustar el tamaño de las columnas automáticamente
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}