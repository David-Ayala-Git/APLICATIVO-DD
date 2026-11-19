package dd.conexiones.dd_ms_auth.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateTenantRequest(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "^[a-z0-9-]{3,60}$", message = "slug debe ser minusculas, numeros y guiones")
        String slug
) {
}
