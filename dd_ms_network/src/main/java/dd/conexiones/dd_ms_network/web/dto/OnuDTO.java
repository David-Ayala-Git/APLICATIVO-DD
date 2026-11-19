package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.OnuStatus;
import dd.conexiones.dd_ms_network.domain.model.enums.OnuVendor;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OnuDTO(
        UUID id,
        String serial,
        OnuVendor vendor,
        String oltName,
        String ponPort,
        Double rxPower,
        OnuStatus status,
        UUID clientId,
        OffsetDateTime createdAt
) {
}
