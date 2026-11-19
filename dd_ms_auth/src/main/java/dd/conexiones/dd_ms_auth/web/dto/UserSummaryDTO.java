package dd.conexiones.dd_ms_auth.web.dto;

import dd.conexiones.dd_ms_auth.domain.model.enums.Role;

import java.util.UUID;

public record UserSummaryDTO(
        UUID id,
        String email,
        String fullName,
        Role role,
        UUID tenantId,
        /** Rol personalizado asignado (Modulos > Roles), o null si solo rige el enum. */
        UUID customRoleId
) {
}
