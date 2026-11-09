package usta.parking.application.port.out;

import usta.parking.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;

// Puerto de salida: persistencia de vehículos
public interface VehicleRepository {

    // Crea el vehículo (sin id) o actualiza uno existente (con id)
    Vehicle save(Vehicle vehicle);

    Optional<Vehicle> findById(Long id);

    // La placa es única y se busca ya normalizada (mayúsculas)
    Optional<Vehicle> findByPlate(String plate);

    boolean existsByPlate(String plate);

    List<Vehicle> findAll();
}
