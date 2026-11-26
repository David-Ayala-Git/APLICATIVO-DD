package dd.conexiones.dd_ms_crm.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateClientRequest(
        String clientCode,
        @NotBlank String fullName,
        @NotBlank String document,
        String phone,
        String address,
        UUID planId
) {
}
