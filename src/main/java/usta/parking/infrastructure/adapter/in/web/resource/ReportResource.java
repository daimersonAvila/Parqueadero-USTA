package usta.parking.infrastructure.adapter.in.web.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;
import usta.parking.application.exception.ResourceNotFoundException;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.infrastructure.adapter.in.web.report.HistoryReport;
import usta.parking.infrastructure.adapter.in.web.report.InsideVehiclesReport;
import usta.parking.infrastructure.adapter.in.web.report.OccupancyReport;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// Descarga de reportes PDF: son las rutas a las que apuntan los botones "Descargar PDF"
@Path("/reports")
@Tag(name = "Reportes", description = "Descarga de reportes en PDF")
public class ReportResource {

    private static final String PDF = "application/pdf";

    private final OccupancyReport occupancyReport;
    private final InsideVehiclesReport insideReport;
    private final HistoryReport historyReport;

    public ReportResource(OccupancyReport occupancyReport,
                          InsideVehiclesReport insideReport,
                          HistoryReport historyReport) {
        this.occupancyReport = occupancyReport;
        this.insideReport = insideReport;
        this.historyReport = historyReport;
    }

    // Ocupación por zona (botón de la página principal)
    @GET
    @Path("/occupancy")
    @Produces(PDF)
    @Operation(summary = "Descargar PDF de ocupación por zona")
    public Response occupancy() {
        return download(occupancyReport.generate(), "ocupacion-parqueadero");
    }

    // Vehículos que están adentro; plate y zoneId son los mismos filtros de la pantalla (opcionales)
    @GET
    @Path("/inside")
    @Produces(PDF)
    @Operation(summary = "Descargar PDF de vehículos adentro")
    public Response inside(@RestQuery String plate, @RestQuery String zoneId) {
        return download(insideReport.generate(plate, parseId(zoneId)), "vehiculos-adentro");
    }

    // Historial entre dos fechas (yyyy-MM-dd). Si no llegan, se usan los últimos 7 días
    @GET
    @Path("/history")
    @Produces(PDF)
    @Operation(summary = "Descargar PDF del historial por rango de fechas")
    public Response history(@RestQuery String from, @RestQuery String to, @RestQuery String zoneId) {
        LocalDate end = parseDate(to, LocalDate.now());
        LocalDate start = parseDate(from, end.minusDays(6));
        try {
            return download(historyReport.generate(start, end, parseId(zoneId)), "historial-parqueadero");
        } catch (ResourceNotFoundException e) {
            // Va primero porque ResourceNotFoundException hereda de BusinessRuleException
            return error(Response.Status.NOT_FOUND, e.getMessage());
        } catch (BusinessRuleException e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    // Respuesta que el navegador descarga como archivo
    private Response download(byte[] pdf, String baseName) {
        String fileName = baseName + "-" + LocalDate.now() + ".pdf";
        return Response.ok(pdf, PDF)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .build();
    }

    // Si algo falla se muestra un mensaje claro en vez de una página rota
    private Response error(Response.Status status, String message) {
        return Response.status(status)
                .type(MediaType.TEXT_PLAIN + "; charset=UTF-8")
                .entity(message)
                .build();
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(String value, LocalDate defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }
}
