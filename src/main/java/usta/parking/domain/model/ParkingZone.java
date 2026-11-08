package usta.parking.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Zona del parqueadero con una capacidad máxima para un tipo de vehículo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ParkingZone {

    private Long id;

    // Ej.: "Zona A - Carros"
    private String name;

    // Tipo de vehículo que admite la zona
    private VehicleType vehicleType;

    // Cantidad máxima de vehículos
    private int capacity;

    // Una zona inactiva (cerrada) no recibe vehículos
    @Builder.Default
    private boolean active = true;
}
