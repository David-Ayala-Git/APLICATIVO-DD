package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.RouterStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RouterDTO(
        UUID id,
        String name,
        String host,
        Integer apiPort,
        String apiUser,
        String model,
        RouterStatus status,
        OffsetDateTime createdAt
) {
}
