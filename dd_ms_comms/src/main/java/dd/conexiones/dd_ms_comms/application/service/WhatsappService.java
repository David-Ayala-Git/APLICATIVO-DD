package dd.conexiones.dd_ms_comms.application.service;

import dd.conexiones.dd_ms_comms.web.dto.IncomingWhatsappWebhookRequest;
import dd.conexiones.dd_ms_comms.web.dto.SendWhatsappRequest;
import dd.conexiones.dd_ms_comms.web.dto.WhatsappMessageDTO;

import java.util.List;

public interface WhatsappService {
    List<WhatsappMessageDTO> findAll();
    WhatsappMessageDTO send(SendWhatsappRequest request);
    WhatsappMessageDTO receiveIncoming(IncomingWhatsappWebhookRequest request);
}
