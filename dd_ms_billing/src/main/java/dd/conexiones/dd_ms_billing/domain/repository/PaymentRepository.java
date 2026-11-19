package dd.conexiones.dd_ms_billing.domain.repository;

import dd.conexiones.dd_ms_billing.domain.model.Payment;
import dd.conexiones.dd_ms_billing.domain.model.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /** Mismo criterio que el indice uq_payment_external_reference (V3). */
    Optional<Payment> findByTenantIdAndMethodAndExternalReference(String tenantId, PaymentMethod method,
                                                                  String externalReference);
}
