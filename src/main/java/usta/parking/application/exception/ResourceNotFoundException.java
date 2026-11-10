package usta.parking.application.exception;

import usta.parking.domain.exception.BusinessRuleException;

// Se lanza cuando se busca una zona, un vehículo o un registro que no existe.
// Hereda de BusinessRuleException para que la vista la muestre en una alerta
// igual que las demás reglas de negocio.
public class ResourceNotFoundException extends BusinessRuleException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
