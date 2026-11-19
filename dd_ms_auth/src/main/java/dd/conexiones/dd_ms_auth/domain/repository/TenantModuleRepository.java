package dd.conexiones.dd_ms_auth.domain.repository;

import dd.conexiones.dd_ms_auth.domain.model.TenantModule;
import dd.conexiones.dd_ms_auth.domain.model.TenantModuleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TenantModuleRepository extends JpaRepository<TenantModule, TenantModuleId> {
    List<TenantModule> findById_TenantId(UUID tenantId);
}
