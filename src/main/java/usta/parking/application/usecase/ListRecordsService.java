package usta.parking.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.service.ParkingRulesService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Caso de uso: consultar los registros de entrada y salida (RF-10, RF-11, RF-12, RF-16, RF-18)
@ApplicationScoped
public class ListRecordsService implements ListRecordsUseCase {

    private final ParkingRecordRepository recordRepository;
    private final ParkingRulesService rules = new ParkingRulesService();

    public ListRecordsService(ParkingRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public List<ParkingRecord> listInside() {
        return recordRepository.findActive();
    }

    @Override
    public Optional<ParkingRecord> findInsideByPlate(String plate) {
        String cleanPlate = PlateUtils.clean(plate);
        if (cleanPlate.isEmpty()) {
            return Optional.empty();
        }
        return recordRepository.findActiveByPlate(cleanPlate);
    }

    @Override
    public List<ParkingRecord> history(LocalDateTime from, LocalDateTime to, Long zoneId) {
        if (from == null || to == null) {
            throw new BusinessRuleException("Las fechas del historial son obligatorias");
        }
        if (from.isAfter(to)) {
            throw new BusinessRuleException("La fecha inicial no puede ser posterior a la fecha final");
        }
        return recordRepository.findByEntryBetween(from, to, zoneId);
    }

    // RN-06: la permanencia la calcula el dominio
    @Override
    public long minutesStayed(ParkingRecord record) {
        return rules.minutesStayed(record);
    }
}
