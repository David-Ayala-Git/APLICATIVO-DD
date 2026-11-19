package dd.conexiones.dd_ms_network.domain.exception;

public class DuplicateSerialException extends RuntimeException {
    public DuplicateSerialException(String serial) {
        super("Ya existe un equipo/ONU con el serial: " + serial);
    }
}
