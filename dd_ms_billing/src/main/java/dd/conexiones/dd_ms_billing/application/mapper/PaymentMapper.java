package dd.conexiones.dd_ms_billing.application.mapper;

import dd.conexiones.dd_ms_billing.domain.model.Payment;
import dd.conexiones.dd_ms_billing.web.dto.PaymentDTO;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDTO toDto(Payment payment) {
        return new PaymentDTO(payment.getId(), payment.getInvoiceId(), payment.getAmount(), payment.getMethod(),
                payment.getStatus(), payment.getExternalReference(), payment.getPaidAt());
    }
}
