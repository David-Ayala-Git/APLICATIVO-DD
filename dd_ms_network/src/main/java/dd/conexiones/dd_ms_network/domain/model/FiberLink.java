package dd.conexiones.dd_ms_network.domain.model;

import dd.conexiones.dd_ms_network.domain.model.enums.FiberType;
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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "network", name = "fiber_link")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
public class FiberLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(length = 120)
    private String name;

    @Column(name = "from_node_id", nullable = false)
    private UUID fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private UUID toNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fiber_type", nullable = false, length = 30)
    private FiberType fiberType = FiberType.DISTRIBUTION;

    @Column(nullable = false, length = 20)
    private String color = "#06b6d4";

    /** Puntos intermedios opcionales del trazado serializados como "lat,lon;lat,lon". */
    @Column(name = "waypoints_data", columnDefinition = "TEXT")
    private String waypointsData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
