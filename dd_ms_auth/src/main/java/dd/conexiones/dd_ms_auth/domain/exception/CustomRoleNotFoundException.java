package dd.conexiones.dd_ms_auth.domain.exception;

public class CustomRoleNotFoundException extends RuntimeException {
    public CustomRoleNotFoundException(String identifier) {
        super("Rol no encontrado: " + identifier);
    }
}
