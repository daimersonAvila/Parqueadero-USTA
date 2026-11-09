package usta.parking.application.port.in;

import usta.parking.domain.model.ParkingZone;

import java.util.List;

// Puerto de entrada: gestionar las zonas del parqueadero (RF-13 y RF-15)
public interface ManageZonesUseCase {

    ParkingZone create(ParkingZone zone);

    // Actualiza nombre, tipo y capacidad (no permite bajar la capacidad de los vehículos adentro)
    ParkingZone update(Long id, ParkingZone zone);

    // Abre o cierra la zona
    ParkingZone setActive(Long id, boolean active);

    ParkingZone findById(Long id);

    List<ParkingZone> findAll();
}
