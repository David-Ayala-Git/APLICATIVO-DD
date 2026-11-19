package dd.conexiones.dd_ms_auth.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class TenantModuleId implements Serializable {

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "module_key", nullable = false, length = 40)
    private String moduleKey;

    public TenantModuleId(UUID tenantId, String moduleKey) {
        this.tenantId = tenantId;
        this.moduleKey = moduleKey;
    }
}
