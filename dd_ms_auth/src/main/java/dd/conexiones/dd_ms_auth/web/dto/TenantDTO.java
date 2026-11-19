package dd.conexiones.dd_ms_auth.web.dto;

import dd.conexiones.dd_ms_auth.domain.model.enums.TenantPlan;
import dd.conexiones.dd_ms_auth.domain.model.enums.TenantStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record TenantDTO(
        UUID id,
        String name,
        String slug,
        TenantPlan plan,
        TenantStatus status,
        String brandPrimary,
        String brandAccent,
        OffsetDateTime createdAt,
        List<TenantModuleDTO> modules
) {
}
