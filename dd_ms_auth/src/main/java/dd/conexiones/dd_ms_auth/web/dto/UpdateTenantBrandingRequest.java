package dd.conexiones.dd_ms_auth.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Colores de marca del proveedor (#RRGGBB). El front los aplica como variables
 * CSS: el patron estricto evita que un valor arbitrario termine inyectado en estilos.
 */
public record UpdateTenantBrandingRequest(
        @NotNull @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "debe ser un color #RRGGBB") String brandPrimary,
        @NotNull @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "debe ser un color #RRGGBB") String brandAccent
) {
}
