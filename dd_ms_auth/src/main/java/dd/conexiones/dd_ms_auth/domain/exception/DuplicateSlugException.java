package dd.conexiones.dd_ms_auth.domain.exception;

public class DuplicateSlugException extends RuntimeException {
    public DuplicateSlugException(String slug) {
        super("Ya existe un tenant con el slug: " + slug);
    }
}
