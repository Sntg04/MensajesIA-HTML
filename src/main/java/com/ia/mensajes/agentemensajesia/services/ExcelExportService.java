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
        String[] columns = {"ID", "Texto del Mensaje", "Clasificación", "Confianza", "Fecha de Procesamiento"};
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Mensajes Procesados");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            
            int rowIdx = 1;
            for (Mensaje mensaje : mensajes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(mensaje.getId());
                row.createCell(1).setCellValue(mensaje.getTexto());
                row.createCell(2).setCellValue(mensaje.getClasificacion());
                row.createCell(3).setCellValue(String.format("%.2f%%", mensaje.getConfianza() * 100));
                
                Cell dateCell = row.createCell(4);
                if (mensaje.getFechaProcesamiento() != null) {
                     CellStyle dateCellStyle = workbook.createCellStyle();
                     CreationHelper createHelper = workbook.getCreationHelper();
                     dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy HH:mm:ss"));
                     dateCell.setCellValue(mensaje.getFechaProcesamiento());
                     dateCell.setCellStyle(dateCellStyle);
                }
            }

            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}