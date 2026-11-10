package usta.parking.infrastructure.adapter.in.web.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestQuery;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.application.port.in.ManageZonesUseCase;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.RecordStatus;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.HistoryPageDto;
import usta.parking.infrastructure.adapter.in.web.dto.HistoryRowDto;
import usta.parking.infrastructure.adapter.in.web.dto.PageLinkDto;
import usta.parking.infrastructure.adapter.in.web.mapper.DisplayFormatter;
import usta.parking.infrastructure.adapter.in.web.mapper.HistoryWebMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

// Pantalla "Historial y reportes": registros por rango de fechas y zona, con botones de PDF (RF-16 a RF-18)
@Path("/history")
@Tag(name = "Historial", description = "Consulta de entradas y salidas por rango de fechas")
public class HistoryResource {

    // Filas por página de la tabla
    private static final int PAGE_SIZE = 10;

    private final ListRecordsUseCase listRecords;
    private final ManageZonesUseCase manageZones;
    private final HistoryWebMapper mapper;
    private final DisplayFormatter formatter;
    private final Template history;

    public HistoryResource(ListRecordsUseCase listRecords,
                           ManageZonesUseCase manageZones,
                           HistoryWebMapper mapper,
                           DisplayFormatter formatter,
                           Template history) {
        this.listRecords = listRecords;
        this.manageZones = manageZones;
        this.mapper = mapper;
        this.formatter = formatter;
        this.history = history;
    }

    // from y to llegan como yyyy-MM-dd. Si no vienen, se usan los últimos 7 días (igual que el PDF)
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Mostrar el historial por rango de fechas y zona")
    public TemplateInstance show(@RestQuery String from,
                                 @RestQuery String to,
                                 @RestQuery String zoneId,
                                 @RestQuery String page) {
        LocalDate end = parseDate(to, LocalDate.now());
        LocalDate start = parseDate(from, end.minusDays(6));
        return render(start, end, parseId(zoneId), parsePage(page));
    }

    private TemplateInstance render(LocalDate start, LocalDate end, Long zoneId, int requestedPage) {
        AlertDto alert = null;
        List<ParkingRecord> records = List.of();
        try {
            // Los dos extremos incluyen el día completo
            records = listRecords.history(start.atStartOfDay(), end.atTime(LocalTime.MAX), zoneId);
        } catch (BusinessRuleException e) {
            // Por ejemplo, cuando "Desde" es posterior a "Hasta"
            alert = AlertDto.error(e.getMessage());
        }

        // Resumen del rango (los mismos datos que muestra el PDF)
        long inside = records.stream().filter(r -> r.getStatus() == RecordStatus.ACTIVE).count();
        OptionalDouble average = records.stream()
                .filter(r -> r.getStatus() == RecordStatus.FINISHED)
                .mapToLong(listRecords::minutesStayed)
                .average();
        String averageText = average.isPresent() ? formatter.duration(Math.round(average.getAsDouble())) : "-";

        // Paginación
        int totalPages = Math.max(1, (records.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(1, requestedPage), totalPages);
        int from = (current - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, records.size());

        List<HistoryRowDto> rows = records.subList(from, to).stream()
                .map(r -> mapper.toRow(r, listRecords.minutesStayed(r)))
                .toList();

        List<PageLinkDto> pages = new ArrayList<>();
        for (int number = 1; number <= totalPages; number++) {
            pages.add(new PageLinkDto(number, listUri(start, end, zoneId, number), number == current));
        }
        String previous = current > 1 ? listUri(start, end, zoneId, current - 1) : null;
        String next = current < totalPages ? listUri(start, end, zoneId, current + 1) : null;

        HistoryPageDto dto = new HistoryPageDto(
                rows,
                records.size(),
                records.size() == 1 ? "registro" : "registros",
                (int) inside,
                averageText,
                rows.size(),
                totalPages,
                pages,
                previous,
                next,
                current,
                start.toString(),
                end.toString(),
                zoneId,
                mapper.toZoneOptions(manageZones.findAll()),
                pdfUri(start, end, zoneId),
                "/reports/occupancy",
                alert);
        return history.data("page", dto);
    }

    // Dirección de la lista conservando fechas, zona y página (solo agrega lo que tiene valor)
    private String listUri(LocalDate start, LocalDate end, Long zoneId, int page) {
        UriBuilder builder = UriBuilder.fromPath("/history")
                .queryParam("from", start.toString())
                .queryParam("to", end.toString());
        if (zoneId != null) {
            builder.queryParam("zoneId", zoneId);
        }
        if (page > 1) {
            builder.queryParam("page", page);
        }
        return builder.build().toString();
    }

    // Enlace del PDF con los mismos filtros de la pantalla
    private String pdfUri(LocalDate start, LocalDate end, Long zoneId) {
        UriBuilder builder = UriBuilder.fromPath("/reports/history")
                .queryParam("from", start.toString())
                .queryParam("to", end.toString());
        if (zoneId != null) {
            builder.queryParam("zoneId", zoneId);
        }
        return builder.build().toString();
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

    // Número de página (1 si no viene o no es válido)
    private int parsePage(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            return (int) Math.min(Long.parseLong(value.trim()), Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            return 1;
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
