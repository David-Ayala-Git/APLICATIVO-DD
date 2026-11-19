package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.RouterOsIds;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record QueueLimitRequest(
        @NotBlank @Pattern(regexp = RouterOsIds.PATTERN,
                message = "solo letras, numeros y . _ @ - (sin espacios)") String queueId,
        // 10 Gbps: por encima es un error de digitacion, no un plan real
        @NotNull @Positive @Max(10_000_000) Integer uploadKbps,
        @NotNull @Positive @Max(10_000_000) Integer downloadKbps
) {
}
