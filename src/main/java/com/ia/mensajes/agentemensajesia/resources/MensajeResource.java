package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.EstadisticaMensaje;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.model.PaginatedResponse;
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

    public static class JobStatus {
        private String status;
        private int progress;
        public JobStatus(String status, int progress) { this.status = status; this.progress = progress; }
        public String getStatus() { return status; }
        public int getProgress() { return progress; }
        public void setStatus(String status) { this.status = status; }
        public void setProgress(int progress) { this.progress = progress; }
    }

    private final MensajeService mensajeService = new MensajeService();
    private final ExcelExportService excelExportService = new ExcelExportService();
    
    public static final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
        try {
            final byte[] fileBytes = uploadedInputStream.readAllBytes();
            final String loteId = java.util.UUID.randomUUID().toString();
            
            jobStatuses.put(loteId, new JobStatus("INICIANDO", 0));

            new Thread(() -> {
                try (InputStream safeInputStream = new ByteArrayInputStream(fileBytes)) {
                    mensajeService.procesarYGuardarMensajesDesdeExcel(safeInputStream, loteId);
                    jobStatuses.put(loteId, new JobStatus("COMPLETADO", 100));
                } catch (Exception e) {
                    jobStatuses.put(loteId, new JobStatus("FALLIDO", 0));
                    e.printStackTrace();
                }
            }).start();

            return Response.status(Response.Status.ACCEPTED)
                           .entity(Map.of("mensaje", "Archivo recibido.", "loteId", loteId))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(Map.of("error", "Error crítico al leer el archivo subido."))
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
    }

    @GET
    @Path("/lotes/{loteId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoteStatus(@PathParam("loteId") String loteId) {
        JobStatus status = jobStatuses.getOrDefault(loteId, new JobStatus("NO_ENCONTRADO", 0));
        return Response.ok(status).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMensajesPaginados(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        try {
            PaginatedResponse<Mensaje> paginatedResponse = mensajeService.obtenerMensajesPaginado(page, size);
            return Response.ok(paginatedResponse).build();
        } catch(Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(Map.of("error", "Error al obtener mensajes paginados"))
                           .build();
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