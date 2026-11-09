package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

// Datos de la pantalla "Vehículos adentro"
@TemplateData
public record InsidePageDto(List<InsideRowDto> rows,
                            int totalInside,          // vehículos adentro (sin filtrar)
                            String totalLabel,        // "vehículo" o "vehículos"
                            int filteredCount,        // vehículos que cumplen el filtro
                            int shownCount,           // filas que se ven en esta página
                            int totalPages,
                            List<PageLinkDto> pages,
                            String previousHref,      // nulo si no hay página anterior
                            String nextHref,          // nulo si no hay página siguiente
                            int currentPage,          // para los campos ocultos de los formularios
                            String filterPlate,
                            Long filterZoneId,
                            List<ZoneOptionDto> zones,
                            AlertDto alert) {         // puede ser nulo (sin mensaje)
}
