package dd.conexiones.dd_ms_comms.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAiAgentRequest(
        @NotBlank String name,
        @NotBlank String channel,
        @NotBlank String systemPrompt,
        String tools
) {
}
