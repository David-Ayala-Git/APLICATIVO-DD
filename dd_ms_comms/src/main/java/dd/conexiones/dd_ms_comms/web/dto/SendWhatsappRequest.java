package dd.conexiones.dd_ms_comms.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SendWhatsappRequest(
        @NotBlank String phone,
        @NotBlank String templateName,
        @NotEmpty List<String> bodyParams
) {
}
