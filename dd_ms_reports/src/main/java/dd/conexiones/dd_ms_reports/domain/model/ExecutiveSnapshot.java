package dd.conexiones.dd_ms_reports.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Read-model transversal de solo lectura: no es dueno de datos propios, es una
 * foto calculada periodicamente a partir de ddc_ms_crm (clientes/planes) y
 * ddc_ms_billing (facturacion), guardada aqui para consulta rapida e historica.
 */
@Entity
@Table(schema = "reports", name = "executive_snapshot")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public class ExecutiveSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 7)
    private String period;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal mrr;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal arpu;

    @Column(name = "churn_rate", nullable = false, precision = 6, scale = 4)
    private BigDecimal churnRate;

    @Column(name = "active_clients", nullable = false)
    private Integer activeClients;

    @Column(name = "retired_clients", nullable = false)
    private Integer retiredClients;

    @Column(name = "total_clients", nullable = false)
    private Integer totalClients;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt = OffsetDateTime.now();
}
