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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        // --- INICIO DE LA LÓGICA ASÍNCRONA ---

        // 1. Iniciar el procesamiento en un nuevo hilo (en segundo plano)
        new Thread(() -> {
            try {
                System.out.println("Iniciando procesamiento de archivo en segundo plano: " + fileDetail.getFileName());
                mensajeService.procesarYGuardarMensajesDesdeExcel(uploadedInputStream);
                System.out.println("Procesamiento en segundo plano completado para: " + fileDetail.getFileName());
            } catch (Exception e) {
                System.err.println("Error en el procesamiento en segundo plano del archivo: " + fileDetail.getFileName());
                e.printStackTrace();
            }
        }).start();

        // 2. Devolver una respuesta INMEDIATA al usuario.
        // Usamos el código de estado 202 (Accepted) que significa "Tu petición fue aceptada
        // y se está procesando, pero aún no ha terminado".
        Map<String, String> response = Map.of(
            "mensaje", "Archivo recibido. El procesamiento ha comenzado en segundo plano.",
            "detalle", "Los nuevos mensajes aparecerán en el dashboard en unos minutos."
        );
        
        return Response.status(Response.Status.ACCEPTED).entity(response).build();
        
        // --- FIN DE LA LÓGICA ASÍNCRONA ---
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