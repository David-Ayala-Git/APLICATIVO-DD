package dd.conexiones.mscommon.module;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@AutoConfiguration
public class ModuleAutoConfiguration {

    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModuleAccessClient moduleAccessClient(RestClient.Builder loadBalancedRestClientBuilder) {
        return new ModuleAccessClient(loadBalancedRestClientBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequiresModuleAspect requiresModuleAspect(ModuleAccessClient moduleAccessClient) {
        return new RequiresModuleAspect(moduleAccessClient);
    }
}
