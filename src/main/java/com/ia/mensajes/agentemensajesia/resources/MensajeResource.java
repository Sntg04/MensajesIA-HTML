package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.EstadisticaMensaje;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.services.ExcelExportService;
import com.ia.mensajes.agentemensajesia.services.MensajeService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/mensajes")
public class MensajeResource {

    private final MensajeService mensajeService = new MensajeService();
    private final ExcelExportService excelExportService = new ExcelExportService();

    // Un mapa seguro para hilos para rastrear el estado de los trabajos de procesamiento.
    // En una aplicación más grande, esto podría ir en una base de datos o un servicio de caché.
    private static final Map<String, String> jobStatuses = new ConcurrentHashMap<>();

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        try {
            final byte[] fileBytes = uploadedInputStream.readAllBytes();
            final String loteId = java.util.UUID.randomUUID().toString(); // Generamos el ID aquí para devolverlo

            // Ponemos el trabajo en estado "PROCESANDO"
            jobStatuses.put(loteId, "PROCESANDO");

            new Thread(() -> {
                try {
                    InputStream safeInputStream = new ByteArrayInputStream(fileBytes);
                    mensajeService.procesarYGuardarMensajesDesdeExcel(safeInputStream, loteId); // Pasamos el loteId
                    jobStatuses.put(loteId, "COMPLETADO"); // Marcamos como completado
                    System.out.println("Procesamiento en segundo plano completado para lote: " + loteId);
                } catch (Exception e) {
                    jobStatuses.put(loteId, "FALLIDO"); // Marcamos como fallido
                    System.err.println("Error en el procesamiento en segundo plano del lote: " + loteId);
                    e.printStackTrace();
                }
            }).start();

            // Devolvemos una respuesta inmediata con el ID del lote para que el frontend pueda consultar
            Map<String, String> response = Map.of(
                "mensaje", "Archivo recibido. El procesamiento ha comenzado.",
                "loteId", loteId
            );
            
            return Response.status(Response.Status.ACCEPTED).entity(response).build();

        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(Map.of("error", "Error crítico al leer el archivo subido."))
                           .build();
        }
    }

    // --- NUEVO ENDPOINT PARA CONSULTAR EL ESTADO ---
    @GET
    @Path("/lotes/{loteId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoteStatus(@PathParam("loteId") String loteId) {
        String status = jobStatuses.getOrDefault(loteId, "NO_ENCONTRADO");
        return Response.ok(Map.of("status", status)).build();
    }


    // --- El resto de los métodos no cambian ---
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMensajes() {
        try {
            List<Mensaje> mensajes = mensajeService.obtenerTodosLosMensajes();
            return Response.ok(mensajes).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener mensajes").build();
        }
    }
    
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats() {
        try {
            return Response.ok(mensajeService.calcularEstadisticas()).build();
        } catch(Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al calcular estadísticas").build();
        }
    }
    
    @GET
    @Path("/export")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportMensajes() {
        try {
            List<Mensaje> mensajes = mensajeService.obtenerTodosLosMensajes();
            ByteArrayInputStream excelStream = excelExportService.exportarMensajesAExcel(mensajes);
            
            String fecha = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String header = "attachment; filename=Reporte_Mensajes_" + fecha + ".xlsx";

            return Response.ok(excelStream).header("Content-Disposition", header).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al generar el archivo Excel.").build();
        }
    }
}