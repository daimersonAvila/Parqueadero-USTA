package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Mensaje que se muestra arriba de la pantalla (éxito, error o advertencia)
@TemplateData
public record AlertDto(String type, String icon, String message) {

    public static AlertDto success(String message) {
        return new AlertDto("success", "check-circle-fill", message);
    }

    public static AlertDto error(String message) {
        return new AlertDto("danger", "exclamation-circle-fill", message);
    }

    public static AlertDto warning(String message) {
        return new AlertDto("warning", "exclamation-triangle-fill", message);
    }
}
