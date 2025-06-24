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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MensajeService {

    private final MensajeDAO mensajeDAO = new MensajeDAO();

    /**
     * Procesa un archivo Excel, clasifica los mensajes en paralelo para mayor velocidad,
     * y los guarda en la base de datos.
     * @param inputStream El stream de datos del archivo .xlsx.
     * @param loteId El identificador único para el lote de procesamiento.
     * @return Una lista de las entidades Mensaje que fueron guardadas.
     * @throws Exception Si ocurre un error al leer o procesar el archivo.
     */
    public List<Mensaje> procesarYGuardarMensajesDesdeExcel(InputStream inputStream, String loteId) throws Exception {

        // --- PASO 1: LEER TODAS LAS FILAS DEL EXCEL EN MEMORIA ---
        // Esto es muy rápido porque solo es lectura, no hay procesamiento.
        List<Row> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            sheet.rowIterator().forEachRemaining(rows::add);
            if (!rows.isEmpty()) {
                rows.remove(0); // Quitar la fila del encabezado
            }
        }

        if (rows.isEmpty()) {
            return new ArrayList<>(); // Devolver lista vacía si no hay filas de datos
        }

        // --- PASO 2: PROCESAR LAS FILAS EN PARALELO ---
        // Usamos parallelStream() para dividir el trabajo en múltiples hilos.
        // La clasificación, que es la parte más lenta, ahora ocurre simultáneamente.
        System.out.println("Iniciando procesamiento en paralelo de " + rows.size() + " filas para el lote: " + loteId);
        long startTime = System.currentTimeMillis();

        List<Mensaje> mensajesProcesados = rows.parallelStream()
            .map(row -> {
                // Extraer datos de las columnas específicas
                String app = getCellValueAsString(row.getCell(0)); // Columna A
                String idCliente = getCellValueAsString(row.getCell(1)); // Columna B
                String textoMensaje = getCellValueAsString(row.getCell(7)); // Columna H
                String asesor = getCellValueAsString(row.getCell(9)); // Columna J
                LocalDateTime fechaHora = getCellLocalDateTime(row.getCell(10)); // Columna K

                // Si no hay texto de mensaje, no procesamos esta fila.
                if (textoMensaje == null || textoMensaje.isEmpty()) {
                    return null;
                }
                
                // Obtenemos una instancia del clasificador (es seguro en paralelo gracias al Singleton)
                ClasificadorMensajes clasificador = ClasificadorMensajes.getInstance();
                ResultadoClasificacion resultado = clasificador.clasificar(textoMensaje);

                // Creamos la entidad Mensaje con todos los datos
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

                return nuevoMensaje;
            })
            .filter(Objects::nonNull) // Filtramos las filas que resultaron nulas (sin texto)
            .collect(Collectors.toList());

        long endTime = System.currentTimeMillis();
        System.out.println("Procesamiento en paralelo completado en " + (endTime - startTime) + " ms.");

        // --- PASO 3: GUARDAR TODOS LOS RESULTADOS EN UNA SOLA TRANSACCIÓN ---
        if (!mensajesProcesados.isEmpty()) {
            System.out.println("Guardando " + mensajesProcesados.size() + " mensajes en la base de datos...");
            mensajeDAO.guardarVarios(mensajesProcesados);
        }

        return mensajesProcesados;
    }

    // --- Métodos de Ayuda (Helpers) ---

    /**
     * Lee el valor de una celda de Excel y lo devuelve como un String,
     * sin importar si el tipo de celda es numérico, texto, etc.
     * @param cell La celda a leer.
     * @return El valor de la celda como String, o null si la celda es nula.
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * Intenta leer una fecha/hora de una celda y la devuelve como LocalDateTime.
     * @param cell La celda que contiene la fecha.
     * @return un objeto LocalDateTime o null si no se puede parsear.
     */
    private LocalDateTime getCellLocalDateTime(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo parsear la fecha de la celda " + cell.getAddress() + ". Valor: " + getCellValueAsString(cell));
        }
        return null;
    }

    // --- Otros métodos del servicio ---

    public List<Mensaje> obtenerTodosLosMensajes() {
        return mensajeDAO.buscarTodos();
    }

    public EstadisticaMensaje calcularEstadisticas() {
        return mensajeDAO.getEstadisticas();
    }
}