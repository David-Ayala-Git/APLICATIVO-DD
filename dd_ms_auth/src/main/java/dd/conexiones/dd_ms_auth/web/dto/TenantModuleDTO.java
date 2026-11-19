package dd.conexiones.dd_ms_auth.web.dto;

import java.util.Map;

public record TenantModuleDTO(
        String moduleKey,
        boolean enabled,
        Map<String, Object> config
) {
}
