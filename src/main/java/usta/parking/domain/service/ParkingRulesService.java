package usta.parking.domain.service;

import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.ParkingZone;
import usta.parking.domain.model.RecordStatus;
import usta.parking.domain.model.Vehicle;

import java.time.Duration;
import java.time.LocalDateTime;

// Servicio que aplica las reglas de negocio de la entrada y la salida (RN-02 a RN-07)
public class ParkingRulesService {

    private static final String PLATE_PATTERN = "[A-Z0-9]{5,7}";

    private final AvailabilityService availabilityService = new AvailabilityService();

    // RN-05: la placa se guarda en mayúsculas, sin espacios, con 5 a 7 caracteres alfanuméricos
    public String normalizePlate(String plate) {
        if (plate == null || plate.isBlank()) {
            throw new BusinessRuleException("La placa es obligatoria");
        }
        String normalized = plate.trim().toUpperCase().replace("-", "").replace(" ", "");
        if (!normalized.matches(PLATE_PATTERN)) {
            throw new BusinessRuleException("La placa debe tener entre 5 y 7 letras o números");
        }
        return normalized;
    }

    // Valida los datos de un vehículo y deja la placa normalizada
    public void validateVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            throw new BusinessRuleException("El vehículo no existe");
        }
        vehicle.setPlate(normalizePlate(vehicle.getPlate()));
        if (vehicle.getVehicleType() == null) {
            throw new BusinessRuleException("El tipo de vehículo es obligatorio");
        }
        if (vehicle.getOwnerName() == null || vehicle.getOwnerName().isBlank()) {
            throw new BusinessRuleException("El nombre del propietario es obligatorio");
        }
        if (vehicle.getOwnerType() == null) {
            throw new BusinessRuleException("El tipo de propietario es obligatorio");
        }
    }

    // RN-07: valida los datos de una zona nueva o editada
    public void validateZone(ParkingZone zone) {
        if (zone == null) {
            throw new BusinessRuleException("La zona no existe");
        }
        if (zone.getName() == null || zone.getName().isBlank()) {
            throw new BusinessRuleException("El nombre de la zona es obligatorio");
        }
        if (zone.getVehicleType() == null) {
            throw new BusinessRuleException("El tipo de vehículo de la zona es obligatorio");
        }
        if (zone.getCapacity() <= 0) {
            throw new BusinessRuleException("La capacidad debe ser mayor que 0");
        }
    }

    // RN-07 (RF-15): no se puede bajar la capacidad por debajo de los vehículos que hay adentro
    public void validateCapacityChange(int newCapacity, int vehiclesInside) {
        if (newCapacity <= 0) {
            throw new BusinessRuleException("La capacidad debe ser mayor que 0");
        }
        if (newCapacity < vehiclesInside) {
            throw new BusinessRuleException("No se puede reducir la capacidad a " + newCapacity
                    + ": hay " + vehiclesInside + " vehículos adentro");
        }
    }

    // Registra la entrada aplicando RN-02, RN-03 y RN-04. Devuelve el registro (aún sin guardar).
    //  alreadyInside:  el vehículo ya tiene un registro ACTIVE
    //  vehiclesInside: cuántos vehículos hay adentro en esa zona
    public ParkingRecord registerEntry(Vehicle vehicle, ParkingZone zone,
                                       boolean alreadyInside, int vehiclesInside) {
        return registerEntry(vehicle, zone, alreadyInside, vehiclesInside, LocalDateTime.now());
    }

    public ParkingRecord registerEntry(Vehicle vehicle, ParkingZone zone,
                                       boolean alreadyInside, int vehiclesInside, LocalDateTime now) {
        if (vehicle == null) {
            throw new BusinessRuleException("El vehículo no existe");
        }
        if (zone == null) {
            throw new BusinessRuleException("La zona no existe");
        }
        // RN-02: un vehículo no puede estar dos veces adentro
        if (alreadyInside) {
            throw new BusinessRuleException("El vehículo " + vehicle.getPlate() + " ya se encuentra dentro del parqueadero");
        }
        // RN-03: zona inactiva o llena
        if (!zone.isActive()) {
            throw new BusinessRuleException("La zona " + zone.getName() + " está cerrada");
        }
        if (availabilityService.freeSpaces(zone, vehiclesInside) == 0) {
            throw new BusinessRuleException("La zona " + zone.getName() + " está llena, no hay cupos disponibles");
        }
        // RN-04: el tipo de vehículo debe coincidir con el de la zona
        if (vehicle.getVehicleType() != zone.getVehicleType()) {
            throw new BusinessRuleException("La zona " + zone.getName() + " no admite este tipo de vehículo");
        }

        return ParkingRecord.builder()
                .vehicle(vehicle)
                .zone(zone)
                .entryTime(now)
                .status(RecordStatus.ACTIVE)
                .build();
    }

    // Registra la salida: cierra el registro (RN-06)
    public ParkingRecord registerExit(ParkingRecord record) {
        return registerExit(record, LocalDateTime.now());
    }

    public ParkingRecord registerExit(ParkingRecord record, LocalDateTime now) {
        if (record == null) {
            throw new BusinessRuleException("El registro no existe");
        }
        if (record.getStatus() != RecordStatus.ACTIVE) {
            throw new BusinessRuleException("Este vehículo ya registró su salida");
        }
        if (now.isBefore(record.getEntryTime())) {
            throw new BusinessRuleException("La hora de salida debe ser posterior a la de entrada");
        }
        record.setExitTime(now);
        record.setStatus(RecordStatus.FINISHED);
        return record;
    }

    // RN-06: permanencia en minutos (si sigue adentro, hasta el momento indicado)
    public long minutesStayed(ParkingRecord record) {
        return minutesStayed(record, LocalDateTime.now());
    }

    public long minutesStayed(ParkingRecord record, LocalDateTime now) {
        if (record == null || record.getEntryTime() == null) {
            return 0;
        }
        LocalDateTime end = record.getExitTime() != null ? record.getExitTime() : now;
        return Math.max(0, Duration.between(record.getEntryTime(), end).toMinutes());
    }
}
