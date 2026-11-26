package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.EquipmentStatus;
import dd.conexiones.dd_ms_network.domain.model.enums.EquipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateEquipmentRequest(
        @NotNull EquipmentType type,
        String brand,
        String model,
        @NotBlank String serialNumber,
        EquipmentStatus status,
        UUID assignedClientId
) {
}
