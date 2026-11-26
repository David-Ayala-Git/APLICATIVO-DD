package dd.conexiones.dd_ms_network.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "network", name = "nap_port_assignment")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public class NapPortAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "node_id", nullable = false)
    private UUID nodeId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "port_number", nullable = false)
    private Integer portNumber;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private OffsetDateTime assignedAt;
}
