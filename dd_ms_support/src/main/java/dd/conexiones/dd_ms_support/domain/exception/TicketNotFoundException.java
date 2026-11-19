package dd.conexiones.dd_ms_support.domain.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String identifier) {
        super("Ticket no encontrado: " + identifier);
    }
}
