package dd.conexiones.dd_ms_support.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTechnicianRequest(
        @NotBlank String fullName,
        String phone
) {
}
