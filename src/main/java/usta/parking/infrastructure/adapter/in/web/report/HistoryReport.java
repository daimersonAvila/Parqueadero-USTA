package usta.parking.infrastructure.adapter.in.web.report;

import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.application.port.in.ManageZonesUseCase;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.domain.model.RecordStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.OptionalDouble;

// Reporte PDF 3: historial por rango de fechas (botón "Descargar historial (PDF)")
@ApplicationScoped
public class HistoryReport {

    private final ListRecordsUseCase listRecords;
    private final ManageZonesUseCase manageZones;

    public HistoryReport(ListRecordsUseCase listRecords, ManageZonesUseCase manageZones) {
        this.listRecords = listRecords;
        this.manageZones = manageZones;
    }

    // from y to incluyen el día completo; zoneId es opcional (null = todas las zonas)
    public byte[] generate(LocalDate from, LocalDate to, Long zoneId) {
        if (from == null || to == null) {
            throw new BusinessRuleException("Las fechas del historial son obligatorias");
        }
        List<ParkingRecord> records = listRecords.history(from.atStartOfDay(), to.atTime(LocalTime.MAX), zoneId);
        String zoneName = zoneId == null ? "Todas" : manageZones.findById(zoneId).getName();

        long inside = records.stream().filter(r -> r.getStatus() == RecordStatus.ACTIVE).count();
        // El promedio se calcula con los registros que ya terminaron (los que siguen adentro no tienen salida)
        OptionalDouble average = records.stream()
                .filter(r -> r.getStatus() == RecordStatus.FINISHED)
                .mapToLong(listRecords::minutesStayed)
                .average();
        String averageText = average.isPresent() ? ReportLabels.stay(Math.round(average.getAsDouble())) : "-";

        return PdfSupport.build(PageSize.A4.rotate(), document -> {
            PdfSupport.addTitle(document, "Historial de entradas y salidas",
                    "Del " + from.format(PdfSupport.DATE) + " al " + to.format(PdfSupport.DATE)
                            + " · Zona: " + zoneName + " · Generado el " + PdfSupport.now());

            document.add(PdfSupport.stats(
                    new String[]{"Registros", "Vehículos adentro", "Permanencia promedio"},
                    new String[]{String.valueOf(records.size()), String.valueOf(inside), averageText}));

            if (records.isEmpty()) {
                PdfSupport.addEmptyNote(document, "No hay registros en el rango de fechas seleccionado.");
                return;
            }

            PdfPTable table = PdfSupport.table(new float[]{1.4f, 3.0f, 2.3f, 2.0f, 2.0f, 1.8f, 1.6f},
                    "Placa", "Propietario", "Zona", "Entrada", "Salida", "Permanencia", "Estado");
            int index = 0;
            for (ParkingRecord record : records) {
                PdfSupport.row(table, index++,
                        record.getVehicle().getPlate(),
                        record.getVehicle().getOwnerName(),
                        record.getZone().getName(),
                        record.getEntryTime().format(PdfSupport.DATE_TIME),
                        record.getExitTime() == null ? "-" : record.getExitTime().format(PdfSupport.DATE_TIME),
                        ReportLabels.stay(listRecords.minutesStayed(record)),
                        ReportLabels.recordStatus(record.getStatus()));
            }
            document.add(table);
        });
    }
}
