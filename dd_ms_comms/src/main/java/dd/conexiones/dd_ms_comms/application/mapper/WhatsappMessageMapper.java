package dd.conexiones.dd_ms_comms.application.mapper;

import dd.conexiones.dd_ms_comms.domain.model.WhatsappMessage;
import dd.conexiones.dd_ms_comms.web.dto.WhatsappMessageDTO;
import org.springframework.stereotype.Component;

@Component
public class WhatsappMessageMapper {

    public WhatsappMessageDTO toDto(WhatsappMessage message) {
        return new WhatsappMessageDTO(message.getId(), message.getPhone(), message.getDirection(),
                message.getBody(), message.getTemplateName(), message.getStatus(), message.getSentAt(),
                message.getCreatedAt());
    }
}
