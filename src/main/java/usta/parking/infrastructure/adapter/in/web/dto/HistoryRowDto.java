package usta.parking.infrastructure.adapter.in.web.dto;

import io.quarkus.qute.TemplateData;

// Una fila de la tabla del historial
@TemplateData
public record HistoryRowDto(String plate,
                            String ownerName,
                            String zoneName,
                            String entryTime,      // ej.: "08/11 07:05"
                            String exitTime,       // "-" si sigue adentro
                            String stay,           // permanencia, ej.: "3 h 12 min"
                            String statusLabel,    // "ADENTRO" o "FINALIZADO"
                            String statusClass) {  // rec-active o rec-finished
}
