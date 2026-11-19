package dd.conexiones.dd_ms_comms.infrastructure.client;

import dd.conexiones.dd_ms_comms.domain.exception.WhatsappSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Cliente de la WhatsApp Business Cloud API (Meta Graph API).
 * Requiere un Phone Number ID y un token permanente reales (Meta for Developers);
 * sin credenciales validas el envio fallara con WhatsappSendException, que es el
 * comportamiento esperado en este entorno de desarrollo (sin cuenta de WhatsApp Business).
 */
@Component
public class WhatsAppClient {

    private final RestClient restClient;
    private final String phoneNumberId;

    public WhatsAppClient(
            @Value("${whatsapp.token:}") String token,
            @Value("${whatsapp.phone-number-id:}") String phoneNumberId,
            @Value("${whatsapp.api-base-url:https://graph.facebook.com/v20.0}") String baseUrl
    ) {
        this.phoneNumberId = phoneNumberId;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    public void sendTemplate(String toPhone, String templateName, List<String> bodyParams) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", toPhone,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", "es"),
                        "components", List.of(Map.of(
                                "type", "body",
                                "parameters", bodyParams.stream()
                                        .map(p -> Map.of("type", "text", "text", p))
                                        .toList()
                        ))
                )
        );

        try {
            restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new WhatsappSendException(e.getMessage(), e);
        }
    }
}
