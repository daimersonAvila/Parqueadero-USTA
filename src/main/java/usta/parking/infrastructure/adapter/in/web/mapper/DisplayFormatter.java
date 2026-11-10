package usta.parking.infrastructure.adapter.in.web.mapper;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Da formato a horas y tiempos para mostrarlos en las vistas
@ApplicationScoped
public class DisplayFormatter {

    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HOUR_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DAY_AND_HOUR = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    // Hora de entrada: solo la hora si fue hoy; si fue otro día, también la fecha
    public String entryTime(LocalDateTime time) {
        if (time == null) {
            return "-";
        }
        if (time.toLocalDate().equals(LocalDate.now())) {
            return time.format(HOUR);
        }
        return time.format(DAY_AND_HOUR);
    }

    public String hour(LocalDateTime time) {
        return time == null ? "-" : time.format(HOUR);
    }

    public String hourWithSeconds(LocalDateTime time) {
        return time == null ? "-" : time.format(HOUR_SECONDS);
    }

    // Permanencia: 47 -> "47 min", 135 -> "2 h 15 min"
    public String duration(long minutes) {
        long safe = Math.max(0, minutes);
        if (safe < 60) {
            return safe + " min";
        }
        return String.format("%d h %02d min", safe / 60, safe % 60);
    }

    // "cupo libre" o "cupos libres" según la cantidad
    public String freeSpacesLabel(int freeSpaces) {
        return freeSpaces == 1 ? "cupo libre" : "cupos libres";
    }
}
