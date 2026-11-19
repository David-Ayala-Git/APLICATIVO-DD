package dd.conexiones.mscommon.automation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * automation.n8n.webhook-base-url vacio = eventos apagados (desarrollo sin n8n).
 * En compose: http://n8n:5678/webhook y el mismo AUTOMATION_SECRET que valida n8n.
 */
@AutoConfiguration
public class AutomationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AutomationEvents automationEvents(
            @Value("${automation.n8n.webhook-base-url:${AUTOMATION_WEBHOOK_BASE_URL:}}") String baseUrl,
            @Value("${automation.n8n.secret:${AUTOMATION_SECRET:}}") String secret) {
        return new AutomationEvents(baseUrl, secret);
    }
}
