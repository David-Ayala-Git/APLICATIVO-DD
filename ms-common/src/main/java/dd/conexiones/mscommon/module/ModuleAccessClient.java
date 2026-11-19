package dd.conexiones.mscommon.module;

import org.springframework.web.client.RestClient;

/**
 * Consulta a ddc_ms_auth (via Consul, sin pasar por el gateway) si un modulo
 * esta habilitado para un tenant. dd-ms-auth es el dueno de auth.tenant_module;
 * los demas servicios no tienen acceso directo a ese schema.
 */
public class ModuleAccessClient {

    private record ModuleStatusResponse(String moduleKey, boolean enabled) {
    }

    private final RestClient restClient;

    public ModuleAccessClient(RestClient.Builder loadBalancedRestClientBuilder) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl("http://dd-ms-auth").build();
    }

    public boolean isEnabled(String tenantSlug, String moduleKey) {
        ModuleStatusResponse response = restClient.get()
                .uri("/api/auth/tenants/by-slug/{slug}/modules/{moduleKey}", tenantSlug, moduleKey)
                .retrieve()
                .body(ModuleStatusResponse.class);
        return response != null && response.enabled();
    }
}
