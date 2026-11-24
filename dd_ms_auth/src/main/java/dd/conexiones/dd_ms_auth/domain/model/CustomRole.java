package dd.conexiones.dd_ms_auth.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Rol personalizado de un proveedor: un nombre y una lista de claves de
 * pagina que ese rol puede ver en la consola (p. ej. "billing", "tickets").
 * Convive con el enum fijo {@link dd.conexiones.dd_ms_auth.domain.model.enums.Role}
 * de AppUser - ese enum sigue siendo el nivel administrativo (quien puede
 * gestionar el tenant); este rol solo afina la navegacion dentro de la consola
 * y ningun otro microservicio lo valida todavia.
 */
@Entity
@Table(schema = "dd_auth", name = "custom_role")
@Getter
@Setter
public class CustomRole {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<String> permissions = List.of();

    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
