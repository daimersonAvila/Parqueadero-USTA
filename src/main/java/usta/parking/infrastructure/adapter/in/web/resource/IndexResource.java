package usta.parking.infrastructure.adapter.in.web.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.parking.application.port.in.CheckAvailabilityUseCase;
import usta.parking.infrastructure.adapter.in.web.mapper.AvailabilityWebMapper;

// Página principal: cupos libres de cada zona (RF-01 a RF-03)
@Path("/")
@Tag(name = "Disponibilidad", description = "Cupos libres de cada zona del parqueadero")
public class IndexResource {

    private final CheckAvailabilityUseCase checkAvailability;
    private final AvailabilityWebMapper mapper;
    private final Template index;

    public IndexResource(CheckAvailabilityUseCase checkAvailability,
                         AvailabilityWebMapper mapper,
                         Template index) {
        this.checkAvailability = checkAvailability;
        this.mapper = mapper;
        this.index = index;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Operation(summary = "Mostrar la disponibilidad de cupos por zona")
    public TemplateInstance show() {
        return index.data("page", mapper.toPage(checkAvailability.getSummary()));
    }
}
