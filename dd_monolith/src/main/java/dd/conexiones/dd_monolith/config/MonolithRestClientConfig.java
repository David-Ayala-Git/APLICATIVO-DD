package dd.conexiones.dd_monolith.config;

import dd.conexiones.dd_ms_auth.application.service.TenantService;
import dd.conexiones.mscommon.module.ModuleAccessClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Configura un unico RestClient.Builder @LoadBalanced con token interno efimero
 * para llamadas loopback (CrmClient, ToolExecutor, CrmReportClient) y resuelve
 * ModuleAccessClient directamente en memoria contra TenantService.
 */
@Configuration
public class MonolithRestClientConfig {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final String internalToken;

    public MonolithRestClientConfig() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        this.internalToken = HexFormat.of().formatHex(bytes);
    }

    public String getInternalToken() {
        return internalToken;
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder()
                .requestInterceptor((request, body, execution) -> {
                    request.getHeaders().set(INTERNAL_TOKEN_HEADER, internalToken);
                    return execution.execute(request, body);
                });
    }

    @Bean
    public ModuleAccessClient moduleAccessClient(RestClient.Builder loadBalancedRestClientBuilder,
                                                 TenantService tenantService) {
        return new ModuleAccessClient(loadBalancedRestClientBuilder) {
            @Override
            public boolean isEnabled(String tenantSlug, String moduleKey) {
                return tenantService.checkModule(tenantSlug, moduleKey).enabled();
            }
        };
    }
}
