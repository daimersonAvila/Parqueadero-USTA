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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.application.port.in.ManageZonesUseCase;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.ParkingZone;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneFormDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneRowDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZonesPageDto;
import usta.parking.infrastructure.adapter.in.web.mapper.ZonesWebMapper;

import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Pantalla "Zonas": crear, editar, abrir y cerrar zonas del parqueadero (RF-13 y RF-15)
@Path("/zones")
@Tag(name = "Zonas", description = "Gestión de las zonas del parqueadero")
public class ZonesResource {

    private final ManageZonesUseCase manageZones;
    private final ListRecordsUseCase listRecords;
    private final ZonesWebMapper mapper;
    private final Template zones;

    public ZonesResource(ManageZonesUseCase manageZones,
                         ListRecordsUseCase listRecords,
                         ZonesWebMapper mapper,
                         Template zones) {
        this.manageZones = manageZones;
        this.listRecords = listRecords;
        this.mapper = mapper;
        this.zones = zones;
    }

    // "edit" abre el formulario con una zona; "saved" y "action" llegan después de guardar
    // y sirven para armar el mensaje de éxito
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Mostrar las zonas del parqueadero")
    public TemplateInstance show(@RestQuery String edit,
                                 @RestQuery String saved,
                                 @RestQuery String action) {
        AlertDto alert = null;
        if (saved != null && !saved.isBlank()) {
            alert = mapper.toSuccessAlert(saved, action);
        }

        ZoneFormDto form = mapper.emptyForm();
        Long editId = parseId(edit);
        if (editId != null) {
            try {
                form = mapper.toForm(manageZones.findById(editId));
            } catch (BusinessRuleException e) {
                // Incluye ResourceNotFoundException (la zona ya no existe)
                alert = AlertDto.error(e.getMessage());
            }
        }
        return render(form, alert);
    }

    // El interruptor "Zona activa" solo se envía cuando está activado
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Crear una zona nueva")
    public Response create(@RestForm String name,
                           @RestForm String vehicleType,
                           @RestForm String capacity,
                           @RestForm String active) {
        ZoneFormDto form = mapper.toForm(null, name, vehicleType, capacity, active != null);
        try {
            ParkingZone saved = manageZones.create(mapper.toDomain(form));
            return redirectSaved(saved.getName(), "created");
        } catch (BusinessRuleException e) {
            return showError(form, e);
        }
    }

    // Al editar no se cambia si la zona está abierta o cerrada: eso se hace con el interruptor de la tabla
    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Actualizar nombre, tipo y capacidad de una zona")
    public Response update(@RestPath Long id,
                           @RestForm String name,
                           @RestForm String vehicleType,
                           @RestForm String capacity) {
        ZoneFormDto form = mapper.toForm(id, name, vehicleType, capacity, true);
        try {
            ParkingZone saved = manageZones.update(id, mapper.toDomain(form));
            return redirectSaved(saved.getName(), "updated");
        } catch (BusinessRuleException e) {
            return showError(form, e);
        }
    }

    // Abre o cierra la zona (RF-15); "active" llega como "true" o "false"
    @POST
    @Path("/{id}/active")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Abrir o cerrar una zona")
    public Response setActive(@RestPath Long id, @RestForm String active) {
        boolean open = "true".equalsIgnoreCase(active);
        try {
            ParkingZone saved = manageZones.setActive(id, open);
            return redirectSaved(saved.getName(), open ? "opened" : "closed");
        } catch (BusinessRuleException e) {
            return showError(mapper.emptyForm(), e);
        }
    }

    // Se redirige para que al recargar la página no se repita la acción
    private Response redirectSaved(String zoneName, String action) {
        URI next = UriBuilder.fromPath("/zones")
                .queryParam("saved", zoneName)
                .queryParam("action", action)
                .build();
        return Response.seeOther(next).build();
    }

    // Se vuelve a mostrar el formulario con lo que escribió la persona
    private Response showError(ZoneFormDto form, BusinessRuleException e) {
        return Response.ok(render(form, AlertDto.error(e.getMessage())))
                .type(MediaType.TEXT_HTML)
                .build();
    }

    private TemplateInstance render(ZoneFormDto form, AlertDto alert) {
        List<ParkingZone> all = manageZones.findAll();
        Map<Long, Integer> inside = countInsideByZone();

        // Se cuentan los vehículos de todas las zonas, también las cerradas
        List<ZoneRowDto> rows = all.stream()
                .sorted(Comparator.comparing(ParkingZone::getName, String.CASE_INSENSITIVE_ORDER))
                .map(z -> mapper.toRow(z, inside.getOrDefault(z.getId(), 0),
                        "/zones?edit=" + z.getId()))
                .toList();

        ZonesPageDto dto = new ZonesPageDto(
                rows,
                all.size(),
                all.size() == 1 ? "zona" : "zonas",
                form,
                form.id() != null,
                mapper.formTitle(form),
                mapper.formAction(form),
                mapper.submitLabel(form),
                mapper.vehicleTypeOptions(),
                alert);
        return zones.data("page", dto);
    }

    // Vehículos adentro por zona: clave = id de la zona
    private Map<Long, Integer> countInsideByZone() {
        Map<Long, Integer> counts = new HashMap<>();
        for (ParkingRecord record : listRecords.listInside()) {
            counts.merge(record.getZone().getId(), 1, Integer::sum);
        }
        return counts;
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
}
