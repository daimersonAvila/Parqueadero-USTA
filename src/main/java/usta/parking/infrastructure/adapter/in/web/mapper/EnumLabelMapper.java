package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.domain.model.OwnerType;
import usta.parking.domain.model.VehicleType;
import usta.parking.domain.model.ZoneStatus;
import usta.parking.infrastructure.adapter.in.web.dto.SelectOptionDto;

import java.util.Arrays;
import java.util.List;

// Convierte los enums del dominio en textos, íconos y estilos para las vistas
@ApplicationScoped
public class EnumLabelMapper {

    public String vehicleTypeLabel(VehicleType type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case CAR -> "Carro";
            case MOTORCYCLE -> "Moto";
        };
    }

    // Clase del ícono de Bootstrap Icons
    public String vehicleTypeIcon(VehicleType type) {
        if (type == VehicleType.MOTORCYCLE) {
            return "bi-scooter";
        }
        return "bi-car-front-fill";
    }

    public String ownerTypeLabel(OwnerType type) {
        if (type == null) {
            return "";
        }
        return switch (type) {
            case STUDENT -> "Estudiante";
            case TEACHER -> "Docente";
            case DIRECTIVE -> "Directivo";
            case STAFF -> "Administrativo";
            case VISITOR -> "Visitante";
        };
    }

    // Clase CSS de la etiqueta de color del rol
    public String ownerTypeCssClass(OwnerType type) {
        if (type == null) {
            return "role-staff";
        }
        return switch (type) {
            case STUDENT -> "role-student";
            case TEACHER -> "role-teacher";
            case DIRECTIVE -> "role-directive";
            case STAFF -> "role-staff";
            case VISITOR -> "role-visitor";
        };
    }

    public String statusLabel(ZoneStatus status) {
        return switch (status) {
            case AVAILABLE -> "DISPONIBLE";
            case FEW_SPACES -> "POCOS CUPOS";
            case FULL -> "LLENA";
        };
    }

    // Sufijo de las clases CSS: status-available, status-few, status-full
    public String statusCssClass(ZoneStatus status) {
        return switch (status) {
            case AVAILABLE -> "available";
            case FEW_SPACES -> "few";
            case FULL -> "full";
        };
    }

    public List<SelectOptionDto> vehicleTypeOptions() {
        return Arrays.stream(VehicleType.values())
                .map(type -> new SelectOptionDto(type.name(), vehicleTypeLabel(type)))
                .toList();
    }

    public List<SelectOptionDto> ownerTypeOptions() {
        return Arrays.stream(OwnerType.values())
                .map(type -> new SelectOptionDto(type.name(), ownerTypeLabel(type)))
                .toList();
    }
}
