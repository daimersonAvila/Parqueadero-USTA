package usta.parking.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import usta.parking.application.exception.ResourceNotFoundException;
import usta.parking.application.port.in.RegisterExitUseCase;
import usta.parking.application.port.out.ParkingRecordRepository;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.service.ParkingRulesService;

// Caso de uso: registrar la salida de un vehículo (RF-09)
@ApplicationScoped
public class RegisterExitService implements RegisterExitUseCase {

    private final ParkingRecordRepository recordRepository;
    private final ParkingRulesService rules = new ParkingRulesService();

    public RegisterExitService(ParkingRecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    @Transactional
    public ParkingRecord registerExit(Long recordId) {
        if (recordId == null) {
            throw new ResourceNotFoundException("El registro no existe");
        }
        ParkingRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("El registro no existe"));
        return recordRepository.save(rules.registerExit(record));
    }

    @Override
    @Transactional
    public ParkingRecord registerExitByPlate(String plate) {
        String cleanPlate = PlateUtils.clean(plate);
        ParkingRecord record = recordRepository.findActiveByPlate(cleanPlate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El vehículo " + cleanPlate + " no se encuentra dentro del parqueadero"));
        return recordRepository.save(rules.registerExit(record));
    }
}
