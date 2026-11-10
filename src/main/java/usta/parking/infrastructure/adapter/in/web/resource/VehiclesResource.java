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
import usta.parking.application.port.in.ManageVehiclesUseCase;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.Vehicle;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.PageLinkDto;
import usta.parking.infrastructure.adapter.in.web.dto.VehicleFormDto;
import usta.parking.infrastructure.adapter.in.web.dto.VehicleRowDto;
import usta.parking.infrastructure.adapter.in.web.dto.VehiclesPageDto;
import usta.parking.infrastructure.adapter.in.web.mapper.VehiclesWebMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Pantalla "Vehículos": lista, búsqueda por placa, registro y edición (RF-11 y RF-14)
@Path("/vehicles")
@Tag(name = "Vehículos", description = "Gestión de los vehículos de la comunidad universitaria")
public class VehiclesResource {

    // Filas por página de la tabla
    private static final int PAGE_SIZE = 10;

    private final ManageVehiclesUseCase manageVehicles;
    private final VehiclesWebMapper mapper;
    private final Template vehicles;

    public VehiclesResource(ManageVehiclesUseCase manageVehicles,
                            VehiclesWebMapper mapper,
                            Template vehicles) {
        this.manageVehicles = manageVehicles;
        this.mapper = mapper;
        this.vehicles = vehicles;
    }

    // plate y page son la búsqueda y la paginación; "edit" abre el formulario con un vehículo;
    // "saved" y "action" llegan después de guardar y sirven para armar el mensaje de éxito
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Mostrar los vehículos registrados")
    public TemplateInstance show(@RestQuery String plate,
                                 @RestQuery String page,
                                 @RestQuery String edit,
                                 @RestQuery String saved,
                                 @RestQuery String action) {
        AlertDto alert = null;
        if (saved != null && !saved.isBlank()) {
            alert = mapper.toSuccessAlert(saved, action);
        }

        VehicleFormDto form = mapper.emptyForm();
        Long editId = parseId(edit);
        if (editId != null) {
            try {
                form = mapper.toForm(manageVehicles.findById(editId));
            } catch (BusinessRuleException e) {
                // Incluye ResourceNotFoundException (el vehículo ya no existe)
                alert = AlertDto.error(e.getMessage());
            }
        }
        return render(plate, parsePage(page), form, alert);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Registrar un vehículo nuevo")
    public Response create(@RestForm String plate,
                           @RestForm String vehicleType,
                           @RestForm String ownerName,
                           @RestForm String ownerType) {
        VehicleFormDto form = mapper.toForm(null, plate, vehicleType, ownerName, ownerType);
        try {
            Vehicle saved = manageVehicles.create(mapper.toDomain(form));
            return redirectSaved(saved.getPlate(), "created");
        } catch (BusinessRuleException e) {
            return showError(form, e);
        }
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Actualizar un vehículo existente")
    public Response update(@RestPath Long id,
                           @RestForm String plate,
                           @RestForm String vehicleType,
                           @RestForm String ownerName,
                           @RestForm String ownerType) {
        VehicleFormDto form = mapper.toForm(id, plate, vehicleType, ownerName, ownerType);
        try {
            Vehicle saved = manageVehicles.update(id, mapper.toDomain(form));
            return redirectSaved(saved.getPlate(), "updated");
        } catch (BusinessRuleException e) {
            return showError(form, e);
        }
    }

    // Se redirige para que al recargar la página no se guarde dos veces
    private Response redirectSaved(String plate, String action) {
        URI next = UriBuilder.fromPath("/vehicles")
                .queryParam("saved", plate)
                .queryParam("action", action)
                .build();
        return Response.seeOther(next).build();
    }

    // Se vuelve a mostrar el formulario con lo que escribió la persona
    private Response showError(VehicleFormDto form, BusinessRuleException e) {
        return Response.ok(render(null, 1, form, AlertDto.error(e.getMessage())))
                .type(MediaType.TEXT_HTML)
                .build();
    }

    private TemplateInstance render(String plate, int requestedPage, VehicleFormDto form, AlertDto alert) {
        List<Vehicle> all = manageVehicles.findAll();
        String plateFilter = normalizeFilter(plate);

        // Filtro por placa (coincidencia parcial), ordenado por placa
        List<Vehicle> filtered = all.stream()
                .filter(v -> plateFilter.isEmpty() || v.getPlate().contains(plateFilter))
                .sorted(Comparator.comparing(Vehicle::getPlate))
                .toList();

        // Paginación
        int totalPages = Math.max(1, (filtered.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int current = Math.min(Math.max(1, requestedPage), totalPages);
        int from = (current - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, filtered.size());

        List<VehicleRowDto> rows = filtered.subList(from, to).stream()
                .map(v -> mapper.toRow(v, listUri(plate, current).queryParam("edit", v.getId()).build().toString()))
                .toList();

        List<PageLinkDto> pages = new ArrayList<>();
        for (int number = 1; number <= totalPages; number++) {
            pages.add(new PageLinkDto(number, listUri(plate, number).build().toString(), number == current));
        }
        String previous = current > 1 ? listUri(plate, current - 1).build().toString() : null;
        String next = current < totalPages ? listUri(plate, current + 1).build().toString() : null;

        VehiclesPageDto dto = new VehiclesPageDto(
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
                form,
                form.id() != null,
                mapper.formTitle(form),
                mapper.formAction(form),
                mapper.submitLabel(form),
                mapper.vehicleTypeOptions(),
                mapper.ownerTypeOptions(),
                alert);
        return vehicles.data("page", dto);
    }

    // Dirección de la lista conservando la búsqueda y la página (solo agrega lo que tiene valor)
    private UriBuilder listUri(String plate, int page) {
        UriBuilder builder = UriBuilder.fromPath("/vehicles");
        if (plate != null && !plate.isBlank()) {
            builder.queryParam("plate", plate);
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
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            return (int) Math.min(Long.parseLong(value.trim()), Integer.MAX_VALUE);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
