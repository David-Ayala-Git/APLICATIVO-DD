package dd.conexiones.dd_ms_network.domain.exception;

public class RouterNotFoundException extends RuntimeException {
    public RouterNotFoundException(String identifier) {
        super("Router no encontrado: " + identifier);
    }
}
