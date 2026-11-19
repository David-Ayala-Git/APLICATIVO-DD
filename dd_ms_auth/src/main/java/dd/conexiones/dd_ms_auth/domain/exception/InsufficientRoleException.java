package dd.conexiones.dd_ms_auth.domain.exception;

public class InsufficientRoleException extends RuntimeException {
    public InsufficientRoleException() {
        super("Tu rol no tiene permiso para gestionar los roles personalizados de este proveedor");
    }

    public InsufficientRoleException(String message) {
        super(message);
    }
}
