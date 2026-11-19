package dd.conexiones.dd_ms_network.domain.exception;

public class NodeNotFoundException extends RuntimeException {
    public NodeNotFoundException(String identifier) {
        super("Nodo no encontrado: " + identifier);
    }
}
