package usta.parking.domain.model;

/** Estado de un registro de parqueo. */
public enum RecordStatus {
    /** El vehículo sigue dentro del parqueadero. */
    ACTIVE,
    /** El vehículo ya salió. */
    FINISHED
}
