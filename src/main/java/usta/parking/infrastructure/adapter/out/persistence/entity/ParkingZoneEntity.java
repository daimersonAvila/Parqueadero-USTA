package usta.parking.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import usta.parking.domain.model.VehicleType;

/** Entidad JPA de la tabla parking_zone (solo existe en infraestructura). */
@Entity
@Table(name = "parking_zone")
@Getter
@Setter
@NoArgsConstructor
public class ParkingZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean active;
}
