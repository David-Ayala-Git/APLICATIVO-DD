package dd.conexiones.dd_ms_network.domain.repository;

import dd.conexiones.dd_ms_network.domain.model.FiberLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FiberLinkRepository extends JpaRepository<FiberLink, UUID> {
    List<FiberLink> findByTenantIdOrderByCreatedAtDesc(String tenantId);
}
