package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.RouterOsIds;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CutClientRequest(
        @NotBlank @Pattern(regexp = RouterOsIds.PATTERN,
                message = "solo letras, numeros y . _ @ - (sin espacios)") String secretId
) {
}
