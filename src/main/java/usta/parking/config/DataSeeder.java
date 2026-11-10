package usta.parking.config;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import usta.parking.domain.model.OwnerType;
import usta.parking.domain.model.RecordStatus;
import usta.parking.domain.model.VehicleType;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingRecordEntity;
import usta.parking.infrastructure.adapter.out.persistence.entity.ParkingZoneEntity;
import usta.parking.infrastructure.adapter.out.persistence.entity.VehicleEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga datos de prueba al arrancar: 4 zonas, 25 vehículos y 40 registros
 * (20 ACTIVE y 20 FINISHED). Solo inserta si las tablas están vacías,
 * así no se duplican datos al reiniciar con strategy=update.
 */
@ApplicationScoped
public class DataSeeder {

    @Inject
    EntityManager em;

    void onStart(@Observes StartupEvent event) {
        QuarkusTransaction.requiringNew().run(this::seed);
    }

    private void seed() {
        Long zonesCount = em.createQuery("select count(z) from ParkingZoneEntity z", Long.class)
                .getSingleResult();
        if (zonesCount > 0) {
            return; // ya hay datos: no se siembra de nuevo
        }

        // ---- Zonas ----
        ParkingZoneEntity zoneA = zone("Zona A - Carros", VehicleType.CAR, 40);
        ParkingZoneEntity zoneB = zone("Zona B - Carros", VehicleType.CAR, 30);
        ParkingZoneEntity zoneDir = zone("Zona Directivas", VehicleType.CAR, 10);
        ParkingZoneEntity zoneMoto = zone("Zona Motos", VehicleType.MOTORCYCLE, 50);

        // ---- Vehículos: 17 carros + 8 motos (placas de 5 a 7 caracteres alfanuméricos) ----
        String[][] carsData = {
                {"RCT001", "Dr. Carlos Mendoza", "DIRECTIVE"},
                {"DIR002", "Dra. Marta Pinzón", "DIRECTIVE"},
                {"DIR003", "Fray Luis Ortega", "DIRECTIVE"},
                {"DIR004", "Ing. Jorge Rojas", "DIRECTIVE"},
                {"DIR005", "Dra. Sandra Gómez", "DIRECTIVE"},
                {"DIR006", "Dr. Andrés Cárdenas", "DIRECTIVE"},
                {"DIR007", "Dra. Patricia Silva", "DIRECTIVE"},
                {"DIR008", "Dr. Hernán Vargas", "DIRECTIVE"},
                {"DIR009", "Dra. Luisa Fonseca", "DIRECTIVE"},
                {"DIR010", "Dr. Camilo Rincón", "DIRECTIVE"},
                {"TNK201", "Ing. Pedro Salamanca", "TEACHER"},
                {"TNK202", "Ing. Diana Cely", "TEACHER"},
                {"TNK203", "Mg. Oscar Barrera", "TEACHER"},
                {"TNK204", "Lic. Rosa Neira", "TEACHER"},
                {"UST301", "Laura Parra", "STUDENT"},
                {"UST302", "Juan Pérez", "STUDENT"},
                {"UST303", "Camila Torres", "STUDENT"}
        };
        String[][] motosData = {
                {"ABC12D", "Santiago Díaz", "STUDENT"},
                {"BCD34E", "Valentina Ruiz", "STUDENT"},
                {"CDE56F", "Mateo Acosta", "STUDENT"},
                {"DEF78G", "Daniela Mora", "STUDENT"},
                {"EFG90H", "Nicolás Peña", "STUDENT"},
                {"FGH12J", "Ing. Ricardo Soto", "TEACHER"},
                {"GHI34K", "Marcela Duarte", "STAFF"},
                {"HIJ56L", "Andrés Visitante", "VISITOR"}
        };

        List<VehicleEntity> cars = new ArrayList<>();
        for (String[] c : carsData) cars.add(vehicle(c, VehicleType.CAR));
        List<VehicleEntity> motos = new ArrayList<>();
        for (String[] m : motosData) motos.add(vehicle(m, VehicleType.MOTORCYCLE));

        LocalDateTime now = LocalDateTime.now();

        // ---- 20 registros ACTIVE ----
        // Zona Directivas: 9 de 10 cupos (queda en estado POCOS)
        for (int i = 0; i < 9; i++) {
            active(cars.get(i), zoneDir, now.minusMinutes(30L + i * 25));
        }
        // Zona A: 5 activos
        for (int i : new int[]{10, 11, 14, 15, 16}) {
            active(cars.get(i), zoneA, now.minusMinutes(40L + i * 7));
        }
        // Zona B: 2 activos
        for (int i : new int[]{12, 13}) {
            active(cars.get(i), zoneB, now.minusMinutes(20L + i * 5));
        }
        // Zona Motos: 4 activos
        for (int i = 0; i < 4; i++) {
            active(motos.get(i), zoneMoto, now.minusMinutes(15L + i * 30));
        }

        // ---- 20 registros FINISHED de los últimos días ----
        for (int i = 0; i < 20; i++) {
            boolean isCar = i % 2 == 0;
            VehicleEntity v = isCar
                    ? cars.get((i / 2) % cars.size())
                    : motos.get((i / 2) % motos.size());
            ParkingZoneEntity z;
            if (!isCar) {
                z = zoneMoto;
            } else if (v.getOwnerType() == OwnerType.DIRECTIVE) {
                z = zoneDir;
            } else {
                z = (i % 4 == 0) ? zoneA : zoneB;
            }
            LocalDateTime entry = now.minusDays(1 + i / 4)
                    .withHour(7 + (i % 6)).withMinute(10 * (i % 6)).withSecond(0).withNano(0);
            ParkingRecordEntity r = record(v, z, entry, RecordStatus.FINISHED);
            r.setExitTime(entry.plusMinutes(90 + i * 17L));
            em.persist(r);
        }
    }

    private ParkingZoneEntity zone(String name, VehicleType type, int capacity) {
        ParkingZoneEntity z = new ParkingZoneEntity();
        z.setName(name);
        z.setVehicleType(type);
        z.setCapacity(capacity);
        z.setActive(true);
        em.persist(z);
        return z;
    }

    private VehicleEntity vehicle(String[] data, VehicleType type) {
        VehicleEntity v = new VehicleEntity();
        v.setPlate(data[0]);
        v.setOwnerName(data[1]);
        v.setOwnerType(OwnerType.valueOf(data[2]));
        v.setVehicleType(type);
        em.persist(v);
        return v;
    }

    private void active(VehicleEntity v, ParkingZoneEntity z, LocalDateTime entry) {
        em.persist(record(v, z, entry, RecordStatus.ACTIVE));
    }

    private ParkingRecordEntity record(VehicleEntity v, ParkingZoneEntity z,
                                       LocalDateTime entry, RecordStatus status) {
        ParkingRecordEntity r = new ParkingRecordEntity();
        r.setVehicle(v);
        r.setZone(z);
        r.setEntryTime(entry);
        r.setStatus(status);
        return r;
    }
}
