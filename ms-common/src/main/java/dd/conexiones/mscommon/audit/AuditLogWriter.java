package dd.conexiones.mscommon.audit;

import dd.conexiones.mscommon.tenant.RequestUserContext;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AuditLogWriter {

    private final JdbcTemplate jdbcTemplate;
    private final String schema;

    public AuditLogWriter(JdbcTemplate jdbcTemplate, String schema) {
        this.jdbcTemplate = jdbcTemplate;
        this.schema = schema;
    }

    /**
     * Transaccion propia (REQUIRES_NEW): el registro de auditoria debe sobrevivir
     * aunque la transaccion de negocio que origino la accion termine en rollback
     * (mismo motivo que WhatsappFailureRecorder en dd_ms_comms).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String detail) {
        jdbcTemplate.update(
                "INSERT INTO " + schema + ".audit_log (id, tenant_id, user_id, user_role, action, detail, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), TenantContext.get(), RequestUserContext.getUserId(),
                RequestUserContext.getUserRole(), action, detail, OffsetDateTime.now());
    }
}
