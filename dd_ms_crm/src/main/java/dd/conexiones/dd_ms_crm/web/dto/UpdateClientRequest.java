package dd.conexiones.dd_ms_crm.web.dto;

import dd.conexiones.dd_ms_crm.domain.model.enums.ClientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateClientRequest(
        @NotBlank String fullName,
        String phone,
        String address,
        UUID planId,
        @NotNull ClientStatus status,
        String ipAddress,
        String onuSerial,
        UUID routerId,
        String pppoeUser
) {
}
