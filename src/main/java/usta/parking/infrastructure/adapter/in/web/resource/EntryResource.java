package usta.parking.infrastructure.adapter.in.web.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;
import usta.parking.application.port.in.CheckAvailabilityUseCase;
import usta.parking.application.port.in.ListRecordsUseCase;
import usta.parking.application.port.in.RegisterEntryUseCase;
import usta.parking.domain.exception.BusinessRuleException;
import usta.parking.domain.model.ParkingRecord;
import usta.parking.infrastructure.adapter.in.web.dto.AlertDto;
import usta.parking.infrastructure.adapter.in.web.dto.EntryFormDto;
import usta.parking.infrastructure.adapter.in.web.dto.ZoneCardDto;
import usta.parking.infrastructure.adapter.in.web.mapper.AvailabilityWebMapper;
import usta.parking.infrastructure.adapter.in.web.mapper.EntryWebMapper;

import java.net.URI;
import java.util.List;

// Pantalla "Registrar entrada" (RF-04 a RF-08)
@Path("/entry")
public class EntryResource {

    private final CheckAvailabilityUseCase checkAvailability;
    private final RegisterEntryUseCase registerEntry;
    private final ListRecordsUseCase listRecords;
    private final AvailabilityWebMapper availabilityMapper;
    private final EntryWebMapper entryMapper;
    private final Template entry;

    public EntryResource(CheckAvailabilityUseCase checkAvailability,
                         RegisterEntryUseCase registerEntry,
                         ListRecordsUseCase listRecords,
                         AvailabilityWebMapper availabilityMapper,
                         EntryWebMapper entryMapper,
                         Template entry) {
        this.checkAvailability = checkAvailability;
        this.registerEntry = registerEntry;
        this.listRecords = listRecords;
        this.availabilityMapper = availabilityMapper;
        this.entryMapper = entryMapper;
        this.entry = entry;
    }

    // "entered" llega después de registrar una entrada: con la placa se arma el mensaje de éxito
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance show(@RestQuery String entered) {
        return render(entryMapper.emptyForm(), successAlert(entered));
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response register(@RestForm String plate,
                             @RestForm String zoneId,
                             @RestForm String newVehicle,
                             @RestForm String vehicleType,
                             @RestForm String ownerName,
                             @RestForm String ownerType) {
        EntryFormDto form = entryMapper.toForm(plate, zoneId, newVehicle, vehicleType, ownerName, ownerType);
        try {
            ParkingRecord record = registerEntry.registerEntry(entryMapper.toCommand(form));
            // Se redirige para que al recargar la página no se registre la entrada dos veces
            URI next = UriBuilder.fromPath("/entry")
                    .queryParam("entered", record.getVehicle().getPlate())
                    .build();
            return Response.seeOther(next).build();
        } catch (BusinessRuleException e) {
            // Se vuelve a mostrar el formulario con lo que escribió la persona
            return Response.ok(render(form, AlertDto.error(e.getMessage())))
                    .type(MediaType.TEXT_HTML)
                    .build();
        }
    }

    private TemplateInstance render(EntryFormDto form, AlertDto alert) {
        List<ZoneCardDto> zones = availabilityMapper.toCards(checkAvailability.getSummary());
        return entry.data("page", entryMapper.toPage(zones, form, alert));
    }

    // Busca el registro recién creado; si no se encuentra simplemente no hay mensaje
    private AlertDto successAlert(String plate) {
        if (plate == null || plate.isBlank()) {
            return null;
        }
        try {
            return listRecords.findInsideByPlate(plate)
                    .map(entryMapper::toSuccessAlert)
                    .orElse(null);
        } catch (BusinessRuleException e) {
            return null;
        }
    }
}
