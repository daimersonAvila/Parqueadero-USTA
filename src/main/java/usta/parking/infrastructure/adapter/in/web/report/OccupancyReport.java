package usta.parking.infrastructure.adapter.in.web.report;

import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import jakarta.enterprise.context.ApplicationScoped;
import usta.parking.application.port.in.CheckAvailabilityUseCase;
import usta.parking.application.port.in.CheckAvailabilityUseCase.AvailabilitySummary;
import usta.parking.application.port.in.CheckAvailabilityUseCase.ZoneAvailability;

// Reporte PDF 1: ocupación por zona (botones "Descargar PDF de ocupación" y "Ocupación por zona (PDF)")
@ApplicationScoped
public class OccupancyReport {

    private final CheckAvailabilityUseCase availability;

    public OccupancyReport(CheckAvailabilityUseCase availability) {
        this.availability = availability;
    }

    // Devuelve los bytes del PDF con los cupos de cada zona abierta
    public byte[] generate() {
        AvailabilitySummary summary = availability.getSummary();

        return PdfSupport.build(PageSize.A4, document -> {
            PdfSupport.addTitle(document, "Ocupación del parqueadero",
                    "Generado el " + PdfSupport.now());

            int occupied = summary.totalCapacity() - summary.totalFree();
            document.add(PdfSupport.stats(
                    new String[]{"Cupos libres", "Cupos ocupados", "Capacidad total", "Zonas abiertas"},
                    new String[]{String.valueOf(summary.totalFree()), String.valueOf(occupied),
                            String.valueOf(summary.totalCapacity()), String.valueOf(summary.zones().size())}));

            if (summary.zones().isEmpty()) {
                PdfSupport.addEmptyNote(document, "No hay zonas abiertas para mostrar.");
                return;
            }

            PdfPTable table = PdfSupport.table(new float[]{3.2f, 1.6f, 1.6f, 1.6f, 1.4f, 1.6f, 1.8f},
                    "Zona", "Tipo", "Capacidad", "Ocupados", "Libres", "Ocupación", "Estado");
            int index = 0;
            for (ZoneAvailability item : summary.zones()) {
                PdfSupport.row(table, index++,
                        item.zone().getName(),
                        ReportLabels.vehicleType(item.zone().getVehicleType()),
                        String.valueOf(item.zone().getCapacity()),
                        String.valueOf(item.vehiclesInside()),
                        String.valueOf(item.freeSpaces()),
                        item.occupancyPercent() + "%",
                        ReportLabels.zoneStatus(item.status()));
            }
            document.add(table);
        });
    }
}
