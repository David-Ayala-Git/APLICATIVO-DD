package dd.conexiones.dd_ms_network.domain.exception;

public class RouterConnectionException extends RuntimeException {
    public RouterConnectionException(String routerName, Throwable cause) {
        super("No se pudo conectar al router '" + routerName + "': " + cause.getMessage(), cause);
    }
}
