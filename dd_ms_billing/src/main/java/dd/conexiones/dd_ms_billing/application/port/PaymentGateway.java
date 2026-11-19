package dd.conexiones.dd_ms_billing.application.port;

import java.util.Map;

/**
 * Pasarela de pagos (Wompi, Mercado Pago, ePayco...). Todavia no hay una
 * elegida: rige NoPaymentGateway, que rechaza todo webhook. Para conectar una
 * real, se implementa esta interfaz en infrastructure/payment con
 * {@code @ConditionalOnProperty(name = "billing.payments.provider", havingValue = "<clave>")}.
 */
public interface PaymentGateway {

    /** Clave de configuracion de la pasarela ("none" mientras no haya una). */
    String key();

    /**
     * Verifica que el webhook venga de verdad de la pasarela (firma/checksum con
     * el secreto de eventos) y lo traduce. Debe lanzar InvalidWebhookSignatureException
     * si la firma no cuadra: este endpoint es publico y marca facturas como pagadas.
     *
     * @param rawBody cuerpo exacto recibido; la firma se calcula sobre el, no sobre
     *                un JSON re-serializado
     * @param headers cabeceras en minusculas
     */
    GatewayPaymentEvent verifyWebhook(String rawBody, Map<String, String> headers);
}
