package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.services.ExcelExportService;
import com.ia.mensajes.agentemensajesia.services.MensajeService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;

@Path("/mensajes")
public class MensajeResource {

    private final MensajeService mensajeService = new MensajeService();
    private final ExcelExportService excelExportService = new ExcelExportService();

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        
        try {
            List<Mensaje> mensajesGuardados = mensajeService.procesarYGuardarMensajesDesdeExcel(uploadedInputStream);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archivo procesado con éxito.");
            response.put("mensajesGuardados", mensajesGuardados.size());
            return Response.ok(response).build();
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No se pudo procesar el archivo: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

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