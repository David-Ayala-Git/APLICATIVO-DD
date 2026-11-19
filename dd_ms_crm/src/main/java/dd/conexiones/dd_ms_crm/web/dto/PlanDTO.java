package dd.conexiones.dd_ms_crm.web.dto;

import dd.conexiones.dd_ms_crm.domain.model.enums.Technology;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PlanDTO(
        UUID id,
        String name,
        Integer downMbps,
        Integer upMbps,
        Technology technology,
        BigDecimal price,
        OffsetDateTime createdAt
) {
}
