package dd.conexiones.dd_ms_network.domain.exception;

public class EquipmentNotFoundException extends RuntimeException {
    public EquipmentNotFoundException(String identifier) {
        super("Equipo no encontrado: " + identifier);
    }
}
