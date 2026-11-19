package dd.conexiones.dd_ms_auth.domain.repository;

import dd.conexiones.dd_ms_auth.domain.model.CustomRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomRoleRepository extends JpaRepository<CustomRole, UUID> {
    List<CustomRole> findByTenantId(UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);

    boolean existsByTenantIdAndNameIgnoreCaseAndIdNot(UUID tenantId, String name, UUID id);
}
