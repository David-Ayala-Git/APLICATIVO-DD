package dd.conexiones.dd_ms_support.web.dto;

import dd.conexiones.dd_ms_support.domain.model.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTicketRequest(
        @NotNull UUID clientId,
        @NotBlank String reason,
        @NotNull Priority priority
) {
}
