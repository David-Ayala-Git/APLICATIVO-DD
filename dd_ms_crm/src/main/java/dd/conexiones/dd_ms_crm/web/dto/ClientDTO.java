package dd.conexiones.dd_ms_crm.web.dto;

import dd.conexiones.dd_ms_crm.domain.model.enums.ClientStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientDTO(
        UUID id,
        String fullName,
        String document,
        String phone,
        String address,
        UUID planId,
        ClientStatus status,
        String ipAddress,
        String onuSerial,
        UUID routerId,
        String pppoeUser,
        OffsetDateTime createdAt
) {
}
