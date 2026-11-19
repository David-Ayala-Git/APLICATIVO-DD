package dd.conexiones.dd_ms_comms.web.dto;

import jakarta.validation.constraints.NotBlank;

public record IncomingWhatsappWebhookRequest(
        @NotBlank String phone,
        @NotBlank String body
) {
}
