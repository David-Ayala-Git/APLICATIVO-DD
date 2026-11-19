package dd.conexiones.dd_ms_support.web.dto;

import dd.conexiones.dd_ms_support.domain.model.enums.TechnicianStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TechnicianDTO(
        UUID id,
        String fullName,
        String phone,
        TechnicianStatus status,
        OffsetDateTime createdAt
) {
}
