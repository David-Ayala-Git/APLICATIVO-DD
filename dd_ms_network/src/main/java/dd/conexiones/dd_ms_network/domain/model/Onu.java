package dd.conexiones.dd_ms_network.domain.model;

import dd.conexiones.dd_ms_network.domain.model.enums.OnuStatus;
import dd.conexiones.dd_ms_network.domain.model.enums.OnuVendor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "network", name = "onu")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public class Onu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 60)
    private String serial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OnuVendor vendor;

    @Column(name = "olt_name", nullable = false, length = 120)
    private String oltName;

    @Column(name = "pon_port", nullable = false, length = 20)
    private String ponPort;

    @Column(name = "rx_power")
    private Double rxPower;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OnuStatus status = OnuStatus.UNAUTHORIZED;

    @Column(name = "client_id")
    private UUID clientId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
