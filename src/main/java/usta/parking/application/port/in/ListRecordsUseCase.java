package usta.parking.application.port.in;

import usta.parking.domain.model.ParkingRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Puerto de entrada: consultar los registros de entrada y salida (RF-10, RF-11, RF-12, RF-16, RF-18)
public interface ListRecordsUseCase {

    // Vehículos que están adentro ahora
    List<ParkingRecord> listInside();

    // Registro activo de una placa (búsqueda en la pantalla "Vehículos adentro")
    Optional<ParkingRecord> findInsideByPlate(String plate);

    // Historial con entrada entre dos fechas; zoneId es opcional (null = todas las zonas)
    List<ParkingRecord> history(LocalDateTime from, LocalDateTime to, Long zoneId);

    // Permanencia en minutos (si sigue adentro, hasta este momento)
    long minutesStayed(ParkingRecord record);
}
