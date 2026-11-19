package dd.conexiones.dd_ms_reports.infrastructure.client;

import dd.conexiones.dd_ms_reports.infrastructure.client.dto.ClientSummary;
import dd.conexiones.dd_ms_reports.infrastructure.client.dto.PlanSummary;
import dd.conexiones.mscommon.role.Roles;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** Lee (nunca escribe) datos de ddc_ms_crm, resuelto vía Consul, para calcular el read-model. */
@Component
public class CrmReportClient {

    private final RestClient restClient;

    public CrmReportClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://dd-ms-crm").build();
    }

    public List<ClientSummary> findClients(String tenantId) {
        ClientSummary[] result = restClient.get()
                .uri("/api/crm/clients")
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Role", Roles.SERVICE)
                .retrieve()
                .body(ClientSummary[].class);
        return result == null ? List.of() : List.of(result);
    }

    public List<PlanSummary> findPlans(String tenantId) {
        PlanSummary[] result = restClient.get()
                .uri("/api/plans")
                .header("X-Tenant-Id", tenantId)
                .header("X-User-Role", Roles.SERVICE)
                .retrieve()
                .body(PlanSummary[].class);
        return result == null ? List.of() : List.of(result);
    }
}
