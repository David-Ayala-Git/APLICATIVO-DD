package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.EquipmentStatus;
import dd.conexiones.dd_ms_network.domain.model.enums.EquipmentType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EquipmentDTO(
        UUID id,
        EquipmentType type,
        String brand,
        String model,
        String serialNumber,
        EquipmentStatus status,
        UUID assignedClientId,
        OffsetDateTime createdAt
) {
}
