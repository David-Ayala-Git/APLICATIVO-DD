package dd.conexiones.dd_ms_crm.web.dto;

import dd.conexiones.dd_ms_crm.domain.model.enums.ContractStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContractDTO(
        UUID id,
        UUID clientId,
        UUID planId,
        ContractStatus status,
        OffsetDateTime signedAt,
        String documentUrl,
        OffsetDateTime createdAt
) {
}
