package dd.conexiones.dd_ms_billing.web.dto;

import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pago recibido fuera de una pasarela (efectivo, Nequi confirmado a mano) que
 * registra un usuario de la consola. El tenant sale de la sesion, no del cuerpo.
 */
public record ManualPaymentRequest(
        @NotNull UUID invoiceId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        @NotNull PaymentMethod method,
        /** Recibo de caja o comprobante; evita registrar dos veces el mismo pago. */
        @NotBlank String externalReference
) {
}
