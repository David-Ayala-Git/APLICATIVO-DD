package dd.conexiones.dd_ms_support.domain.exception;

public class TechnicianNotFoundException extends RuntimeException {
    public TechnicianNotFoundException(String identifier) {
        super("Tecnico no encontrado: " + identifier);
    }
}
