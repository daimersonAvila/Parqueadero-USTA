package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Zona para las listas desplegables de filtro
@TemplateData
public record ZoneOptionDto(Long id, String name) {
}
