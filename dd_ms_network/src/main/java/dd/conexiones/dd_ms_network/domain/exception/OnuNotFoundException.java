package dd.conexiones.dd_ms_network.domain.exception;

public class OnuNotFoundException extends RuntimeException {
    public OnuNotFoundException(String identifier) {
        super("ONU no encontrada: " + identifier);
    }
}
