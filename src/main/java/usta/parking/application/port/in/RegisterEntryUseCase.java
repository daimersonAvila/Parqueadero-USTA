package usta.parking.application.port.in;

import usta.parking.domain.model.OwnerType;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.VehicleType;

// Puerto de entrada: registrar la entrada de un vehículo (RF-04 a RF-08)
public interface RegisterEntryUseCase {

    // Datos del formulario de entrada.
    // Si el vehículo ya existe basta con plate y zoneId; los demás campos solo
    // se usan cuando el vehículo es nuevo (se registra en el mismo paso, RF-05).
    record EntryCommand(String plate, Long zoneId,
                        VehicleType vehicleType, String ownerName, OwnerType ownerType) {
    }

    // Registra la entrada y devuelve el registro creado.
    // Lanza BusinessRuleException si la zona está llena, cerrada, el vehículo ya
    // está adentro o el tipo de vehículo no corresponde a la zona.
    ParkingRecord registerEntry(EntryCommand command);
}
