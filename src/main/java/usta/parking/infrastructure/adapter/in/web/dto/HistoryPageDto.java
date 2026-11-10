package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

import java.util.List;

// Datos de la pantalla "Historial y reportes"
@TemplateData
public record HistoryPageDto(List<HistoryRowDto> rows,
                             int totalRecords,         // registros del rango (sin paginar)
                             String totalLabel,        // "registro" o "registros"
                             int insideCount,          // registros que siguen adentro
                             String averageStay,       // promedio de los que ya salieron, o "-"
                             int shownCount,           // filas que se ven en esta página
                             int totalPages,
                             List<PageLinkDto> pages,
                             String previousHref,      // nulo si no hay página anterior
                             String nextHref,          // nulo si no hay página siguiente
                             int currentPage,
                             String fromDate,          // yyyy-MM-dd, para el campo "Desde"
                             String toDate,            // yyyy-MM-dd, para el campo "Hasta"
                             Long filterZoneId,
                             List<ZoneOptionDto> zones,
                             String historyPdfHref,    // botón "Descargar historial (PDF)"
                             String occupancyPdfHref,  // botón "Ocupación por zona (PDF)"
                             AlertDto alert) {         // puede ser nulo (sin mensaje)
}
