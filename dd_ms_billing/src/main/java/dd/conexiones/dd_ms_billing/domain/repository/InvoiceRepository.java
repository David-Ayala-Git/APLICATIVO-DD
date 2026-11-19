package dd.conexiones.dd_ms_billing.domain.repository;

import dd.conexiones.dd_ms_billing.domain.model.Invoice;
import dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    boolean existsByTenantIdAndClientIdAndPeriod(String tenantId, UUID clientId, String period);

    List<Invoice> findByTenantIdAndStatus(String tenantId, InvoiceStatus status);

    /** Pendientes cuya fecha limite ya paso -> OVERDUE. Devuelve cuantas cambiaron. */
    @Modifying
    @Query("update Invoice i set i.status = dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus.OVERDUE"
            + " where i.tenantId = :tenantId"
            + " and i.status = dd.conexiones.dd_ms_billing.domain.model.enums.InvoiceStatus.PENDING"
            + " and i.dueDate < :today")
    int markOverdue(@Param("tenantId") String tenantId, @Param("today") LocalDate today);
}
