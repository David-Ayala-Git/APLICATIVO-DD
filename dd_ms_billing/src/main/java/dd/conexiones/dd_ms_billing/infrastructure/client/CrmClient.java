package dd.conexiones.dd_ms_billing.infrastructure.client;

import dd.conexiones.dd_ms_billing.infrastructure.client.dto.ClientSummary;
import dd.conexiones.dd_ms_billing.infrastructure.client.dto.PlanSummary;
import dd.conexiones.mscommon.role.Roles;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Cliente REST hacia ddc_ms_crm, resuelto vía Consul (lb://dd-ms-crm).
 * Propaga el tenant actual como header, igual que lo haria el gateway.
 */
@Component
public class CrmClient {

    private final RestClient restClient;

    public CrmClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://dd-ms-crm").build();
    }

    public List<ClientSummary> findActiveClients() {
        ClientSummary[] result = restClient.get()
                .uri("/api/crm/clients")
                .header("X-Tenant-Id", TenantContext.get())
                .header("X-User-Role", Roles.SERVICE)
                .retrieve()
                .body(ClientSummary[].class);

        if (result == null) {
            return List.of();
        }
        return Arrays.stream(result)
                .filter(c -> "ACTIVE".equals(c.status()) && c.planId() != null)
                .toList();
    }

    public PlanSummary findPlan(UUID planId) {
        return restClient.get()
                .uri("/api/plans/{id}", planId)
                .header("X-Tenant-Id", TenantContext.get())
                .header("X-User-Role", Roles.SERVICE)
                .retrieve()
                .body(PlanSummary.class);
    }
}
