package usta.parking.application.port.in;

import usta.parking.domain.model.ParkingRecord;

// Puerto de entrada: registrar la salida de un vehículo (RF-09)
public interface RegisterExitUseCase {

    // Cierra el registro indicado (botón "Registrar salida" de la tabla)
    ParkingRecord registerExit(Long recordId);

    // Cierra el registro activo del vehículo con esa placa
    ParkingRecord registerExitByPlate(String plate);
}
