package usta.parking.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import usta.parking.domain.model.RecordStatus;

import java.time.LocalDateTime;

/** Entidad JPA de la tabla parking_record, con llaves foráneas a vehicle y parking_zone. */
@Entity
@Table(name = "parking_record")
@Getter
@Setter
@NoArgsConstructor
public class ParkingRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private ParkingZoneEntity zone;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    /** Nulo mientras el vehículo sigue adentro. */
    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;
}
