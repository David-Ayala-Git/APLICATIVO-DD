package dd.conexiones.dd_ms_comms.application.impl;

import dd.conexiones.dd_ms_comms.application.mapper.WhatsappMessageMapper;
import dd.conexiones.dd_ms_comms.application.service.WhatsappService;
import dd.conexiones.dd_ms_comms.domain.exception.UnroutableWebhookException;
import dd.conexiones.dd_ms_comms.domain.exception.WhatsappSendException;
import dd.conexiones.dd_ms_comms.domain.model.WhatsappMessage;
import dd.conexiones.dd_ms_comms.domain.model.enums.MessageDirection;
import dd.conexiones.dd_ms_comms.domain.model.enums.MessageStatus;
import dd.conexiones.dd_ms_comms.domain.repository.WhatsappMessageRepository;
import dd.conexiones.dd_ms_comms.infrastructure.client.WhatsAppClient;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_comms.web.dto.IncomingWhatsappWebhookRequest;
import dd.conexiones.dd_ms_comms.web.dto.SendWhatsappRequest;
import dd.conexiones.dd_ms_comms.web.dto.WhatsappMessageDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class WhatsappServiceImpl implements WhatsappService {

    private final WhatsappMessageRepository whatsappMessageRepository;
    private final WhatsappMessageMapper whatsappMessageMapper;
    private final WhatsAppClient whatsAppClient;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final WhatsappFailureRecorder whatsappFailureRecorder;

    @PersistenceContext
    private EntityManager entityManager;

    public WhatsappServiceImpl(WhatsappMessageRepository whatsappMessageRepository,
                                WhatsappMessageMapper whatsappMessageMapper, WhatsAppClient whatsAppClient,
                                TenantFilterEnabler tenantFilterEnabler, WhatsappFailureRecorder whatsappFailureRecorder) {
        this.whatsappMessageRepository = whatsappMessageRepository;
        this.whatsappMessageMapper = whatsappMessageMapper;
        this.whatsAppClient = whatsAppClient;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.whatsappFailureRecorder = whatsappFailureRecorder;
    }

    @Override
    @Transactional(readOnly = true)
    @RequiresModule("comms")
    public List<WhatsappMessageDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return whatsappMessageRepository.findAll().stream().map(whatsappMessageMapper::toDto).toList();
    }

    @Override
    @RequiresModule("comms")
    public WhatsappMessageDTO send(SendWhatsappRequest request) {
        WhatsappMessage message = new WhatsappMessage();
        message.setTenantId(TenantContext.require());
        message.setPhone(request.phone());
        message.setDirection(MessageDirection.OUT);
        message.setTemplateName(request.templateName());
        message.setBody(String.join(" | ", request.bodyParams()));

        try {
            whatsAppClient.sendTemplate(request.phone(), request.templateName(), request.bodyParams());
            message.setStatus(MessageStatus.SENT);
            message.setSentAt(OffsetDateTime.now());
        } catch (WhatsappSendException e) {
            // saveAndFlush aqui no basta: al relanzar la excepcion, @Transactional
            // hace rollback de TODO el metodo (incluido este insert). Se persiste
            // en una transaccion aparte para que el registro de fallo sobreviva.
            message.setStatus(MessageStatus.FAILED);
            whatsappFailureRecorder.recordFailure(message);
            throw e;
        }

        return whatsappMessageMapper.toDto(whatsappMessageRepository.saveAndFlush(message));
    }

    @Override
    public WhatsappMessageDTO receiveIncoming(IncomingWhatsappWebhookRequest request) {
        // Meta llama este webhook sin X-Tenant-Id: el tenant se resuelve buscando
        // la conversacion previa mas reciente con ese numero (ver
        // WhatsappMessageRepository.findTopByPhoneOrderByCreatedAtDesc), ya que
        // esta ruta publica del gateway nunca inyecta el header.
        String tenantId = whatsappMessageRepository.findTopByPhoneOrderByCreatedAtDesc(request.phone())
                .map(WhatsappMessage::getTenantId)
                .orElseThrow(() -> new UnroutableWebhookException(request.phone()));

        WhatsappMessage message = new WhatsappMessage();
        message.setTenantId(tenantId);
        message.setPhone(request.phone());
        message.setDirection(MessageDirection.IN);
        message.setBody(request.body());
        message.setStatus(MessageStatus.RECEIVED);

        return whatsappMessageMapper.toDto(whatsappMessageRepository.saveAndFlush(message));
    }
}
