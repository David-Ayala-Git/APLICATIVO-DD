package dd.conexiones.dd_ms_support.domain.exception;

public class WorkOrderNotFoundException extends RuntimeException {
    public WorkOrderNotFoundException(String identifier) {
        super("Orden de trabajo no encontrada: " + identifier);
    }
}
