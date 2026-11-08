package usta.parking.domain.model;

/** Estado de ocupación de una zona (se calcula, no se guarda en la base de datos). */
public enum ZoneStatus {
    /** Hay cupos suficientes. */
    AVAILABLE,
    /** Quedan pocos cupos. */
    FEW_SPACES,
    /** No quedan cupos o la zona está cerrada. */
    FULL
}
