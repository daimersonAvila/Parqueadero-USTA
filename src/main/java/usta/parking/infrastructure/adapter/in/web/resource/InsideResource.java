package usta.parking.infrastructure.adapter.in.web.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.application.port.in.ManageZonesUseCase;
import usta.parking.application.port.in.RegisterExitUseCase;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.InsidePageDto;
import usta.parking.infrastructure.adapter.in.web.dto.InsideRowDto;
import usta.parking.infrastructure.adapter.in.web.dto.PageLinkDto;
import usta.parking.infrastructure.adapter.in.web.mapper.DisplayFormatter;
import usta.parking.infrastructure.adapter.in.web.mapper.InsideWebMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

// Pantalla "Vehículos adentro": lista, búsqueda y salida con un clic (RF-09 a RF-12)
@Path("/inside")
public class InsideResource {

    // Filas por página de la tabla
    private static final int PAGE_SIZE = 10;

    private final ListRecordsUseCase listRecords;
    private final RegisterExitUseCase registerExit;
    private final ManageZonesUseCase manageZones;
    private final InsideWebMapper mapper;
    private final DisplayFormatter formatter;
    private final Template inside;

    public InsideResource(ListRecordsUseCase listRecords,
                          RegisterExitUseCase registerExit,
                          ManageZonesUseCase manageZones,
                          InsideWebMapper mapper,
                          DisplayFormatter formatter,
                          Template inside) {
        this.listRecords = listRecords;
        this.registerExit = registerExit;
        this.manageZones = manageZones;
        this.mapper = mapper;
        this.formatter = formatter;
        this.inside = inside;
    }

    // plate y zoneId son filtros; "exited" y "minutes" llegan después de registrar una salida
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance show(@RestQuery String plate,
                                 @RestQuery String zoneId,
                                 @RestQuery String page,
                                 @RestQuery String exited,
                                 @RestQuery String minutes) {
        AlertDto alert = null;
        if (exited != null && !exited.isBlank()) {
            alert = AlertDto.success("Salida registrada: " + exited
                    + " permaneció " + formatter.duration(parseNumber(minutes, 0)));
        }
        return render(plate, parseId(zoneId), parsePage(page), alert);
    }

    @POST
    @Path("/{id}/exit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response exit(@RestPath Long id,
                        @RestForm String plate,
                        @RestForm String zoneId,
                        @RestForm String page) {
        try {
            ParkingRecord record = registerExit.registerExit(id);
            long minutes = listRecords.minutesStayed(record);
            // Se redirige para que al recargar la página no se repita la salida
            URI next = listUri(plate, parseId(zoneId), parsePage(page))
                    .queryParam("exited", record.getVehicle().getPlate())
                    .queryParam("minutes", minutes)
                    .build();
            return Response.seeOther(next).build();
        } catch (BusinessRuleException e) {
            return Response.ok(render(plate, parseId(zoneId), parsePage(page),
                            AlertDto.error(e.getMessage())))
                    .type(MediaType.TEXT_HTML)
                    .build();
        }
    }

    private TemplateInstance render(String plate, Long zoneId, int requestedPage, AlertDto alert) {
        List<ParkingRecord> all = listRecords.listInside();
        String plateFilter = normalizeFilter(plate);

        // Filtro por placa (coincidencia parcial) y por zona
        List<ParkingRecord> filtered = all.stream()
                .filter(r -> plateFilter.isEmpty() || r.getVehicle().getPlate().contains(plateFilter))
                .filter(r -> zoneId == null || zoneId.equals(r.getZone().getId()))
                .toList();

        // Paginación
        int totalPages = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(1, requestedPage), totalPages);
        int from = (current - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());

        List<InsideRowDto> rows = filtered.subList(from, to).stream()
                .map(r -> mapper.toRow(r, listRecords.minutesStayed(r)))
                .toList();

        List<PageLinkDto> pages = new ArrayList<>();
        for (int number = 1; number <= totalPages; number++) {
            pages.add(new PageLinkDto(number, listUri(plate, zoneId, number).build().toString(), number == current));
        }
        String previous = current > 1 ? listUri(plate, zoneId, current - 1).build().toString() : null;
        String next = current < totalPages ? listUri(plate, zoneId, current + 1).build().toString() : null;

        InsidePageDto dto = new InsidePageDto(
                rows,
                all.size(),
                all.size() == 1 ? "vehículo" : "vehículos",
                filtered.size(),
                rows.size(),
                totalPages,
                pages,
                previous,
                next,
                current,
                plate == null ? "" : plate,
                zoneId,
                mapper.toZoneOptions(manageZones.findAll()),
                alert);
        return inside.data("page", dto);
    }

    // Dirección de la lista conservando los filtros y la página (solo agrega lo que tiene valor)
    private UriBuilder listUri(String plate, Long zoneId, int page) {
        UriBuilder builder = UriBuilder.fromPath("/inside");
        if (plate != null && !plate.isBlank()) {
            builder.queryParam("plate", plate);
        }
        if (zoneId != null) {
            builder.queryParam("zoneId", zoneId);
        }
        if (page > 1) {
            builder.queryParam("page", page);
        }
        return builder;
    }

    // La placa se guarda en mayúsculas y sin guiones ni espacios (RN-05)
    private String normalizeFilter(String plate) {
        if (plate == null) {
            return "";
        }
        return plate.trim().toUpperCase().replace("-", "").replace(" ", "");
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
        return (int) Math.min(parseNumber(value, 1), Integer.MAX_VALUE);
    }

    private long parseNumber(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
