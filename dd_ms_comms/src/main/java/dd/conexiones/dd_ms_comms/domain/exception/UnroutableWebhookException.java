package dd.conexiones.dd_ms_comms.domain.exception;

public class UnroutableWebhookException extends RuntimeException {
    public UnroutableWebhookException(String phone) {
        super("No hay conversacion previa con el numero " + phone
                + "; no se puede determinar a que tenant pertenece este mensaje entrante "
                + "(se requiere haberle enviado antes un mensaje saliente, o un mapeo "
                + "phone_number_id->tenant para primer contacto)");
    }
}
