package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.OnuVendor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterOnuRequest(
        @NotBlank String serial,
        @NotNull OnuVendor vendor,
        @NotBlank String oltName,
        @NotBlank String ponPort
) {
}
