package dd.conexiones.dd_ms_network.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignNapClientRequest(
        @NotNull UUID clientId,
        Integer portNumber
) {
}
