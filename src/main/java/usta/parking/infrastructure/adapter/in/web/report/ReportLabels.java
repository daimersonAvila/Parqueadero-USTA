package usta.parking.infrastructure.adapter.in.web.report;

import usta.parking.domain.model.OwnerType;
import usta.parking.domain.model.RecordStatus;
import usta.parking.domain.model.VehicleType;
import usta.parking.domain.model.ZoneStatus;

// Textos en español para mostrar los enums del dominio (en los PDF y también reutilizable en las vistas)
public final class ReportLabels {

    private ReportLabels() {
    }

    public static String vehicleType(VehicleType type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case CAR -> "Carro";
            case MOTORCYCLE -> "Moto";
        };
    }

    public static String ownerType(OwnerType type) {
        if (type == null) {
            return "-";
        }
        return switch (type) {
            case STUDENT -> "Estudiante";
            case TEACHER -> "Docente";
            case DIRECTIVE -> "Directivo";
            case STAFF -> "Administrativo";
            case VISITOR -> "Visitante";
        };
    }

    public static String zoneStatus(ZoneStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case AVAILABLE -> "Disponible";
            case FEW_SPACES -> "Pocos cupos";
            case FULL -> "Llena";
        };
    }

    public static String recordStatus(RecordStatus status) {
        if (status == null) {
            return "-";
        }
        return switch (status) {
            case ACTIVE -> "Adentro";
            case FINISHED -> "Finalizado";
        };
    }

    // Permanencia en texto: 135 -> "2 h 15 min", 45 -> "45 min"
    public static String stay(long minutes) {
        long safe = Math.max(0, minutes);
        long hours = safe / 60;
        long mins = safe % 60;
        return hours == 0 ? mins + " min" : hours + " h " + mins + " min";
    }
}
