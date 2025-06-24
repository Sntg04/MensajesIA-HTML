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
            // --- INICIO DE LA SOLUCIÓN ---

            // 1. Leer el stream a un array de bytes ANTES de que termine la petición.
            // Esto copia el contenido completo del archivo a la memoria de la aplicación.
            final byte[] fileBytes = uploadedInputStream.readAllBytes();
            
            // El stream original (uploadedInputStream) será cerrado por el servidor, pero ya no nos importa.

            // 2. Iniciar el procesamiento en un nuevo hilo, pasando los bytes.
            new Thread(() -> {
                try {
                    System.out.println("Iniciando procesamiento de archivo en segundo plano: " + fileDetail.getFileName());
                    // 3. Crear un NUEVO stream a partir de los bytes. Este es seguro de usar.
                    InputStream safeInputStream = new ByteArrayInputStream(fileBytes);
                    
                    // 4. Pasar el stream seguro al servicio para el procesamiento.
                    mensajeService.procesarYGuardarMensajesDesdeExcel(safeInputStream);
                    
                    System.out.println("Procesamiento en segundo plano completado para: " + fileDetail.getFileName());
                } catch (Exception e) {
                    System.err.println("Error en el procesamiento en segundo plano del archivo: " + fileDetail.getFileName());
                    e.printStackTrace();
                }
            }).start();

            // 5. Devolver la respuesta inmediata y correcta al usuario.
            Map<String, String> response = Map.of(
                "mensaje", "Archivo recibido. El procesamiento ha comenzado en segundo plano.",
                "detalle", "Los nuevos mensajes aparecerán en el dashboard en unos minutos."
            );
            
            return Response.status(Response.Status.ACCEPTED).entity(response).build();
            
            // --- FIN DE LA SOLUCIÓN ---

        } catch (IOException e) {
            // Este catch es por si falla la lectura inicial del archivo a bytes.
            e.printStackTrace();
            Map<String, String> errorResponse = Map.of("error", "Error crítico al leer el archivo subido.");
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