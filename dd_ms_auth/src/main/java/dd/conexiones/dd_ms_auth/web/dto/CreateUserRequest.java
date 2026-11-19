package dd.conexiones.dd_ms_auth.web.dto;

import dd.conexiones.dd_ms_auth.domain.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String fullName,
        @NotNull Role role,
        UUID tenantId,
        /** Opcional: rol personalizado (debe existir y ser del mismo tenantId). */
        UUID customRoleId
) {
}
