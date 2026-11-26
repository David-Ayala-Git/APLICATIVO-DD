package dd.conexiones.dd_ms_crm.domain.exception;

public class DuplicateClientCodeException extends RuntimeException {
    public DuplicateClientCodeException(String code) {
        super("Ya existe un cliente con el codigo: " + code);
    }
}
