package dd.conexiones.dd_ms_comms.application.impl;

import dd.conexiones.dd_ms_comms.domain.model.WhatsappMessage;
import dd.conexiones.dd_ms_comms.domain.repository.WhatsappMessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persiste mensajes fallidos en una transaccion propia (REQUIRES_NEW) para que
 * sobrevivan al rollback que Spring aplica en el metodo de envio cuando este
 * relanza la excepcion de fallo (ver WhatsappServiceImpl.send).
 */
@Component
public class WhatsappFailureRecorder {

    private final WhatsappMessageRepository whatsappMessageRepository;

    public WhatsappFailureRecorder(WhatsappMessageRepository whatsappMessageRepository) {
        this.whatsappMessageRepository = whatsappMessageRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(WhatsappMessage message) {
        whatsappMessageRepository.saveAndFlush(message);
    }
}
