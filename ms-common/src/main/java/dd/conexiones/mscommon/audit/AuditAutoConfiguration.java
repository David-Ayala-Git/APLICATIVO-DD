package dd.conexiones.mscommon.audit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogWriter auditLogWriter(JdbcTemplate jdbcTemplate,
                                          @Value("${spring.jpa.properties.hibernate.default_schema}") String schema) {
        return new AuditLogWriter(jdbcTemplate, schema);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditAspect auditAspect(AuditLogWriter auditLogWriter) {
        return new AuditAspect(auditLogWriter);
    }
}
