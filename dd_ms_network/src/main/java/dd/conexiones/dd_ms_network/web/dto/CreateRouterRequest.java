package dd.conexiones.dd_ms_network.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateRouterRequest(
        @NotBlank String name,
        @NotBlank String host,
        @NotNull @Positive Integer apiPort,
        @NotBlank String apiUser,
        @NotBlank String apiPassword,
        String model
) {
}
