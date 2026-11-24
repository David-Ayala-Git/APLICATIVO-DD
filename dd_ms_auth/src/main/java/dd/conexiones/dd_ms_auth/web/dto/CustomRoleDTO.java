package dd.conexiones.dd_ms_auth.web.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CustomRoleDTO(
        UUID id,
        UUID tenantId,
        String name,
        String description,
        List<String> permissions,
        boolean enabled,
        OffsetDateTime createdAt
) {
}
