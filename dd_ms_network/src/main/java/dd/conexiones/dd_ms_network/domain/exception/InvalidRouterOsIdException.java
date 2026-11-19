package dd.conexiones.dd_ms_network.domain.exception;

public class InvalidRouterOsIdException extends RuntimeException {
    public InvalidRouterOsIdException(String value) {
        super("Identificador de RouterOS invalido: solo letras, numeros y . _ @ - (sin espacios)");
    }
}
