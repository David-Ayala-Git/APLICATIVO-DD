package dd.conexiones.dd_ms_auth.domain.exception;

public class DuplicateRoleNameException extends RuntimeException {
    public DuplicateRoleNameException(String name) {
        super("Ya existe un rol con el nombre: " + name);
    }
}
