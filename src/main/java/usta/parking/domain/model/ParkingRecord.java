package usta.parking.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/** Registro de la permanencia de un vehículo en una zona (entrada y salida). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ParkingRecord {

    private Long id;
    private Vehicle vehicle;
    private ParkingZone zone;

    // Momento en que entró el vehículo
    @Builder.Default
    private LocalDateTime entryTime = LocalDateTime.now();

    // Momento en que salió (nulo mientras sigue adentro)
    private LocalDateTime exitTime;

    // ACTIVE mientras está adentro, FINISHED cuando sale
    @Builder.Default
    private RecordStatus status = RecordStatus.ACTIVE;
}
