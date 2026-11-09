package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Botón numerado de la paginación
@TemplateData
public record PageLinkDto(int number, String href, boolean current) {
}
