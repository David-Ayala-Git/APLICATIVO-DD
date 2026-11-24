package dd.conexiones.dd_monolith.config;

import dd.conexiones.mscommon.audit.AuditAspect;
import dd.conexiones.mscommon.audit.AuditLogWriter;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.tenant.RequestUserContext;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * Enruta cada @Audited a la tabla <esquema>.audit_log del dominio propietario del bean,
 * manteniendo paridad exacta con el despliegue en 7 microservicios.
 */
@Configuration
public class MonolithAuditConfig {

    private static final Map<String, String> PACKAGE_TO_SCHEMA = Map.of(
            "dd.conexiones.dd_ms_auth", "dd_auth",
            "dd.conexiones.dd_ms_crm", "crm",
            "dd.conexiones.dd_ms_billing", "billing",
            "dd.conexiones.dd_ms_network", "network",
            "dd.conexiones.dd_ms_support", "support",
            "dd.conexiones.dd_ms_comms", "comms",
            "dd.conexiones.dd_ms_reports", "reports"
    );

    @Bean
    public MonolithAuditLogWriter auditLogWriter(JdbcTemplate jdbcTemplate) {
        return new MonolithAuditLogWriter(jdbcTemplate);
    }

    @Bean
    public AuditAspect auditAspect(MonolithAuditLogWriter auditLogWriter) {
        return new MonolithAuditAspect(auditLogWriter);
    }

    public static class MonolithAuditLogWriter extends AuditLogWriter {

        private final JdbcTemplate jdbcTemplate;

        public MonolithAuditLogWriter(JdbcTemplate jdbcTemplate) {
            super(jdbcTemplate, "dd_auth");
            this.jdbcTemplate = jdbcTemplate;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void recordInSchema(String schema, String action, String detail) {
            jdbcTemplate.update(
                    "INSERT INTO " + schema + ".audit_log (id, tenant_id, user_id, user_role, action, detail, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID(), TenantContext.get(), RequestUserContext.getUserId(),
                    RequestUserContext.getUserRole(), action, detail, OffsetDateTime.now());
        }
    }

    @Aspect
    public static class MonolithAuditAspect extends AuditAspect {

        private final MonolithAuditLogWriter writer;

        public MonolithAuditAspect(MonolithAuditLogWriter writer) {
            super(writer);
            this.writer = writer;
        }

        @Override
        @AfterReturning(pointcut = "@annotation(audited)", argNames = "joinPoint,audited")
        public void audit(JoinPoint joinPoint, Audited audited) {
            String pkg = joinPoint.getTarget().getClass().getPackageName();
            String schema = PACKAGE_TO_SCHEMA.entrySet().stream()
                    .filter(e -> pkg.startsWith(e.getKey()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse("dd_auth");
            String detail = Arrays.toString(joinPoint.getArgs());
            writer.recordInSchema(schema, audited.value(), detail);
        }
    }
}
