package usta.parking.application.port.in;

import usta.parking.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;

// Puerto de entrada: gestionar los vehículos (RF-11 y RF-14)
public interface ManageVehiclesUseCase {

    // Valida y normaliza la placa; lanza BusinessRuleException si ya existe
    Vehicle create(Vehicle vehicle);

    Vehicle update(Long id, Vehicle vehicle);

    Vehicle findById(Long id);

    // Búsqueda por placa (la placa se normaliza antes de buscar)
    Optional<Vehicle> findByPlate(String plate);

    List<Vehicle> findAll();
}
