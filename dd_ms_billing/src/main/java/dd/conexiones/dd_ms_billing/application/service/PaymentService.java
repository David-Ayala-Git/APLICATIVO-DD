package dd.conexiones.dd_ms_billing.application.service;

import dd.conexiones.dd_ms_billing.web.dto.ManualPaymentRequest;
import dd.conexiones.dd_ms_billing.web.dto.PaymentDTO;
import dd.conexiones.dd_ms_billing.web.dto.UpdatePaymentRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {
    List<PaymentDTO> findAll();
    PaymentDTO findById(UUID id);
    PaymentDTO registerManual(ManualPaymentRequest request);
    PaymentDTO update(UUID id, UpdatePaymentRequest request);
    void delete(UUID id);
    PaymentDTO processGatewayWebhook(String rawBody, Map<String, String> headers);
}
