package usta.parking.infrastructure.adapter.in.web.report;

import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.domain.model.ParkingRecord;

import java.util.List;

// Reporte PDF 2: vehículos que están adentro ahora (botón "Descargar PDF" de "Vehículos adentro")
@ApplicationScoped
public class InsideVehiclesReport {

    private final ListRecordsUseCase listRecords;

    public InsideVehiclesReport(ListRecordsUseCase listRecords) {
        this.listRecords = listRecords;
    }

    // plateFilter y zoneId son opcionales: son los mismos filtros de la pantalla (null = sin filtro)
    public byte[] generate(String plateFilter, Long zoneId) {
        String filter = plateFilter == null ? ""
                : plateFilter.trim().toUpperCase().replace("-", "").replace(" ", "");

        List<ParkingRecord> inside = listRecords.listInside().stream()
                .filter(record -> filter.isEmpty() || record.getVehicle().getPlate().contains(filter))
                .filter(record -> zoneId == null || zoneId.equals(record.getZone().getId()))
                .toList();

        return PdfSupport.build(PageSize.A4.rotate(), document -> {
            PdfSupport.addTitle(document, "Vehículos dentro del parqueadero",
                    "Generado el " + PdfSupport.now());

            document.add(PdfSupport.stats(
                    new String[]{"Vehículos adentro"},
                    new String[]{String.valueOf(inside.size())}));

            if (inside.isEmpty()) {
                PdfSupport.addEmptyNote(document, "No hay vehículos adentro con los filtros aplicados.");
                return;
            }

            PdfPTable table = PdfSupport.table(new float[]{1.5f, 1.3f, 3.2f, 1.8f, 2.4f, 2.0f, 1.8f},
                    "Placa", "Tipo", "Propietario", "Rol", "Zona", "Hora de entrada", "Permanencia");
            int index = 0;
            for (ParkingRecord record : inside) {
                PdfSupport.row(table, index++,
                        record.getVehicle().getPlate(),
                        ReportLabels.vehicleType(record.getVehicle().getVehicleType()),
                        record.getVehicle().getOwnerName(),
                        ReportLabels.ownerType(record.getVehicle().getOwnerType()),
                        record.getZone().getName(),
                        record.getEntryTime().format(PdfSupport.DATE_TIME),
                        ReportLabels.stay(listRecords.minutesStayed(record)));
            }
            document.add(table);
        });
    }
}
