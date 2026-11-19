package dd.conexiones.dd_ms_crm.domain.exception;

public class DuplicateClientDocumentException extends RuntimeException {
    public DuplicateClientDocumentException(String document) {
        super("Ya existe un cliente con el documento: " + document);
    }
}
