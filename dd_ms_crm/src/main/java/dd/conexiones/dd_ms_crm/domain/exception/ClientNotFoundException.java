package dd.conexiones.dd_ms_crm.domain.exception;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(String identifier) {
        super("Cliente no encontrado: " + identifier);
    }
}
