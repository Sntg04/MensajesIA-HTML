package com.ia.mensajes.agentemensajesia.services;

import com.ia.mensajes.agentemensajesia.model.Mensaje;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelExportService {
    
    public ByteArrayInputStream exportarMensajesAExcel(List<Mensaje> mensajes) throws IOException {
        String[] columns = {"ID", "Texto del Mensaje", "Clasificación", "Confianza", "Fecha de Procesamiento", "Lote"};
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mensajes Procesados");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            
            CellStyle dateCellStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy HH:mm:ss"));

            int rowIdx = 1;
            for (Mensaje mensaje : mensajes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(mensaje.getId());
                row.createCell(1).setCellValue(mensaje.getTexto());
                row.createCell(2).setCellValue(mensaje.getClasificacion());
                row.createCell(3).setCellValue(String.format("%.2f%%", mensaje.getConfianza() * 100));
                
                Cell dateCell = row.createCell(4);
                if (mensaje.getFechaProcesamiento() != null) {
                     dateCell.setCellValue(mensaje.getFechaProcesamiento());
                     dateCell.setCellStyle(dateCellStyle);
                }
                row.createCell(5).setCellValue(mensaje.getLote());
            }

            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}