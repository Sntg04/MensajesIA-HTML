package com.ia.mensajes.agentemensajesia.resources;

import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.services.MensajeService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/mensajes")
public class MensajeResource {

    private final MensajeService mensajeService;

    public MensajeResource() {
        this.mensajeService = new MensajeService();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "calidad"})
    public Response subirArchivoExcel(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData) {
        
        try {
            List<Mensaje> mensajesProcesados = mensajeService.procesarArchivoExcel(fileInputStream);
            
            if (mensajesProcesados == null || mensajesProcesados.isEmpty()) {
                return Response.ok(Map.of("mensaje", "El archivo estaba vacío o no contenía datos válidos.", "mensajes", List.of(), "loteId", "")).build();
            }

            String loteId = mensajesProcesados.get(0).getLoteCarga();
            Map<String, Object> respuesta = Map.of(
                "loteId", loteId,
                "mensajes", mensajesProcesados
            );

            return Response.ok(respuesta).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = Map.of("error", "No se pudo procesar el archivo: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorResponse).build();
        }
    }

    @GET
    @Path("/alertas")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"admin", "calidad"})
    public Response obtenerMensajesDeAlerta(@QueryParam("lote") String loteId) {
        try {
            List<Mensaje> alertas = mensajeService.obtenerAlertasPorLote(loteId);
            return Response.ok(alertas).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"Error al obtener alertas\"}").build();
        }
    }
    
    @GET
    @Path("/alertas/exportar")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @RolesAllowed({"admin", "calidad"})
    public Response exportarAlertas(@QueryParam("lote") String loteId) {
        try {
            ByteArrayInputStream excelStream = mensajeService.exportarAlertasAExcel(loteId);
            return Response.ok(excelStream)
                    .header("Content-Disposition", "attachment; filename=reporte_de_alertas.xlsx")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al generar el reporte de alertas.").build();
        }
    }
    
    // --- Endpoint para Exportación Completa ---
    @GET
    @Path("/lote/exportar")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @RolesAllowed({"admin", "calidad"})
    public Response exportarLoteCompleto(@QueryParam("lote") String loteId) {
        try {
            if (loteId == null || loteId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("El ID del lote es requerido.").build();
            }
            
            ByteArrayInputStream excelStream = mensajeService.exportarLoteCompletoAExcel(loteId);
            
            // Generar un nombre de archivo dinámico y seguro
            String fileName = "reporte_completo_lote_" + loteId.substring(0, Math.min(loteId.length(), 8)) + ".xlsx";

            // Devolver la respuesta para que el navegador inicie la descarga
            return Response.ok(excelStream)
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"") // comillas por seguridad
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al generar el reporte.").build();
        }
    }
}