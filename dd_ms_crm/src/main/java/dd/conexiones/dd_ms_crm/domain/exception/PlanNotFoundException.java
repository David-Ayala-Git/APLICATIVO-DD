package dd.conexiones.dd_ms_crm.domain.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(String identifier) {
        super("Plan no encontrado: " + identifier);
    }
}
