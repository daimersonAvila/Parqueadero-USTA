package usta.parking.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Vehículo que ingresa al parqueadero de la universidad. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Vehicle {

    private Long id;

    // Placa en mayúsculas (única)
    private String plate;

    private VehicleType vehicleType;

    // Nombre del propietario
    private String ownerName;

    // Estudiante, docente, directivo, administrativo o visitante
    private OwnerType ownerType;
}
