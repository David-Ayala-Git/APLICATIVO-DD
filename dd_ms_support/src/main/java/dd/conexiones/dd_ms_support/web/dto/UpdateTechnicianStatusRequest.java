package dd.conexiones.dd_ms_support.web.dto;

import dd.conexiones.dd_ms_support.domain.model.enums.TechnicianStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTechnicianStatusRequest(
        @NotNull TechnicianStatus status
) {
}
