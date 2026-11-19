package dd.conexiones.mscommon.role;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class RoleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RequiresRoleAspect requiresRoleAspect() {
        return new RequiresRoleAspect();
    }
}
