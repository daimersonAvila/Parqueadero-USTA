package usta.parking.application.usecase;

// Utilidad interna de la capa de aplicación para las búsquedas por placa
final class PlateUtils {

    private PlateUtils() {
    }

    // Limpia el texto de búsqueda: mayúsculas, sin espacios ni guiones.
    // No valida el formato: una búsqueda parcial ("AB") no debe lanzar error.
    static String clean(String plate) {
        if (plate == null) {
            return "";
        }
        return plate.trim().toUpperCase().replace("-", "").replace(" ", "");
    }
}
