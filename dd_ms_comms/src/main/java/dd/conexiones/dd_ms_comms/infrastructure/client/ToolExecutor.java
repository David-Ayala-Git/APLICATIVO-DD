package dd.conexiones.dd_ms_comms.infrastructure.client;

import dd.conexiones.mscommon.role.Roles;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Puente seguro entre el modelo y el resto de la plataforma: el modelo nunca toca la
 * base de datos, solo pide ejecutar una de estas funciones bien definidas. Cada llamada
 * valida y propaga el tenant actual, y se resuelve vía Consul (lb://) hacia el
 * microservicio dueno del dato.
 */
@Component
public class ToolExecutor {

    private final RestClient restClient;

    public ToolExecutor(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.build();
    }

    public String consultarCliente(String tenantId, String clientId) {
        return getJson("http://dd-ms-crm/api/crm/clients/" + clientId, tenantId);
    }

    public String crearTicket(String tenantId, String clientId, String reason, String priority) {
        Map<String, Object> body = Map.of(
                "clientId", clientId,
                "reason", reason,
                "priority", priority
        );
        return postJson("http://dd-ms-support/api/tickets", tenantId, body);
    }

    public String estadoFactura(String tenantId, String invoiceId) {
        return getJson("http://dd-ms-billing/api/billing/invoices/" + invoiceId, tenantId);
    }

    private String getJson(String url, String tenantId) {
        try {
            return restClient.get()
                    .uri(url)
                    .header("X-Tenant-Id", tenantId)
                    .header("X-User-Role", Roles.SERVICE)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String postJson(String url, String tenantId, Map<String, Object> body) {
        try {
            return restClient.post()
                    .uri(url)
                    .header("X-Tenant-Id", tenantId)
                    .header("X-User-Role", Roles.SERVICE)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
