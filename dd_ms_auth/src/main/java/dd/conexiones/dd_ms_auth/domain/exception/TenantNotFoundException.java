package dd.conexiones.dd_ms_auth.domain.exception;

public class TenantNotFoundException extends RuntimeException {
    public TenantNotFoundException(String identifier) {
        super("Tenant no encontrado: " + identifier);
    }
}
