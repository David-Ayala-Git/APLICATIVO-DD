package dd.conexiones.dd_ms_auth.domain.exception;

public class RoleInUseException extends RuntimeException {
    public RoleInUseException(String identifier) {
        super("No se puede eliminar: hay usuarios asignados a este rol (" + identifier + ")");
    }
}
