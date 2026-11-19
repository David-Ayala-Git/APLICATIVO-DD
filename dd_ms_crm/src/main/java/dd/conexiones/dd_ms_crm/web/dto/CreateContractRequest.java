package dd.conexiones.dd_ms_crm.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateContractRequest(
        @NotNull UUID clientId,
        @NotNull UUID planId,
        String documentUrl
) {
}
