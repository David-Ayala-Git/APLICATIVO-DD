package dd.conexiones.dd_ms_crm.domain.exception;

public class PlanInUseException extends RuntimeException {
    public PlanInUseException(String id) {
        super("No se puede eliminar el plan porque tiene clientes asignados. Deshabilitalo o cambia el plan de esos clientes primero.");
    }
}
