package dd.conexiones.dd_ms_network.domain.repository;

import dd.conexiones.dd_ms_network.domain.model.NapPortAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NapPortAssignmentRepository extends JpaRepository<NapPortAssignment, UUID> {
    List<NapPortAssignment> findByTenantIdOrderByPortNumberAsc(String tenantId);
    List<NapPortAssignment> findByNodeIdAndTenantIdOrderByPortNumberAsc(UUID nodeId, String tenantId);
    Optional<NapPortAssignment> findByTenantIdAndClientId(String tenantId, UUID clientId);
    Optional<NapPortAssignment> findByIdAndNodeIdAndTenantId(UUID id, UUID nodeId, String tenantId);
    boolean existsByNodeIdAndPortNumber(UUID nodeId, Integer portNumber);
}
