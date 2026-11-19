package dd.conexiones.dd_ms_comms.domain.exception;

public class WhatsappSendException extends RuntimeException {
    public WhatsappSendException(String reason, Throwable cause) {
        super("No se pudo enviar el mensaje de WhatsApp: " + reason, cause);
    }
}
