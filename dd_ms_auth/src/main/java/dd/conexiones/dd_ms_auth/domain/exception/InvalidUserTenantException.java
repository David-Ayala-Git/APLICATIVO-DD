package dd.conexiones.dd_ms_auth.domain.exception;

/** El rol pedido no cuadra con el tenant: SUPER_ADMIN va sin tenant, el resto con uno existente. */
public class InvalidUserTenantException extends RuntimeException {
    public InvalidUserTenantException(String message) {
        super(message);
    }
}
