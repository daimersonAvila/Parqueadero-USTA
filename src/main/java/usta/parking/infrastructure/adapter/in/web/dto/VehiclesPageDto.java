package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

// Datos de la pantalla "Vehículos"
@TemplateData
public record VehiclesPageDto(List<VehicleRowDto> rows,
                              int totalVehicles,          // vehículos registrados (sin filtrar)
                              String totalLabel,          // "vehículo" o "vehículos"
                              int filteredCount,          // vehículos que cumplen la búsqueda
                              int shownCount,             // filas que se ven en esta página
                              int totalPages,
                              List<PageLinkDto> pages,
                              String previousHref,        // nulo si no hay página anterior
                              String nextHref,            // nulo si no hay página siguiente
                              int currentPage,
                              String filterPlate,
                              VehicleFormDto form,
                              boolean editing,            // true = el formulario edita un vehículo
                              String formTitle,           // "Registrar vehículo" o "Editar vehículo"
                              String formAction,          // dirección a la que se envía el formulario
                              String submitLabel,         // texto del botón de guardar
                              List<SelectOptionDto> vehicleTypes,
                              List<SelectOptionDto> ownerTypes,
                              AlertDto alert) {           // puede ser nulo (sin mensaje)
}
