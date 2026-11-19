package dd.conexiones.dd_ms_network.web.dto;

import jakarta.validation.constraints.NotNull;

public record SignalUpdateRequest(
        @NotNull Double rxPower
) {
}
