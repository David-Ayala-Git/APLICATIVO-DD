package dd.conexiones.dd_ms_billing.infrastructure.payment;

import dd.conexiones.dd_ms_billing.application.port.GatewayPaymentEvent;
import dd.conexiones.dd_ms_billing.application.port.PaymentGateway;
import dd.conexiones.dd_ms_billing.domain.exception.PaymentGatewayNotConfiguredException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Sin pasarela contratada no hay firma que verificar, asi que ningun webhook es
 * confiable: se rechazan todos (503) en lugar de aceptar cualquier POST anonimo
 * como un pago. Los pagos recibidos a mano van por POST /api/payments/manual.
 */
@Component
@ConditionalOnProperty(name = "billing.payments.provider", havingValue = "none", matchIfMissing = true)
public class NoPaymentGateway implements PaymentGateway {

    @Override
    public String key() {
        return "none";
    }

    @Override
    public GatewayPaymentEvent verifyWebhook(String rawBody, Map<String, String> headers) {
        throw new PaymentGatewayNotConfiguredException();
    }
}
