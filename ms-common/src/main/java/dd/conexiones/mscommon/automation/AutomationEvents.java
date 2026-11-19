package dd.conexiones.mscommon.automation;

import dd.conexiones.mscommon.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Avisa a n8n de hechos de negocio (pago confirmado, cliente creado...) para que
 * sus flujos encadenen red + facturacion + WhatsApp. Es "dispara y olvida": si
 * n8n esta caido, el pago igual queda registrado; solo se pierde el aviso (y el
 * flujo diario de corte/reactivacion lo reconcilia).
 *
 * <p>Se envia DESPUES del commit: un evento de una transaccion revertida haria que
 * n8n reactivara a un cliente cuyo pago no existe.
 */
public class AutomationEvents {

    /** Cabecera que el webhook de n8n debe validar (Header Auth). */
    public static final String SECRET_HEADER = "X-DD-Automation-Secret";

    private static final Logger log = LoggerFactory.getLogger(AutomationEvents.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final String secret;

    public AutomationEvents(String baseUrl, String secret) {
        this.restClient = RestClient.create();
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
        this.secret = secret;
    }

    public boolean isEnabled() {
        return !baseUrl.isBlank();
    }

    /** @param event ultimo tramo de la URL del webhook en n8n, p. ej. "payment-confirmed" */
    public void publish(String event, Map<String, Object> payload) {
        if (!isEnabled()) {
            return;
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("event", event);
        body.put("tenantId", TenantContext.get());
        body.putAll(payload);

        Runnable send = () -> Thread.ofVirtual().start(() -> post(event, body));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
        } else {
            send.run();
        }
    }

    private void post(String event, Map<String, Object> body) {
        try {
            restClient.post()
                    .uri(baseUrl + "/" + event)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(SECRET_HEADER, secret == null ? "" : secret)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException e) {
            log.warn("No se pudo avisar a n8n del evento {}: {}", event, e.getMessage());
        }
    }
}
