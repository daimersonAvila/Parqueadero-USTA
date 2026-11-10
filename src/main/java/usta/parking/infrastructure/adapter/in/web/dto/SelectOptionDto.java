package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Opción de una lista desplegable: value es lo que se envía y label lo que se ve
@TemplateData
public record SelectOptionDto(String value, String label) {
}
