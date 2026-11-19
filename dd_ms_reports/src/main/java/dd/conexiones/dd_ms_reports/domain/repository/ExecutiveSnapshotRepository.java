package dd.conexiones.dd_ms_reports.domain.repository;

import dd.conexiones.dd_ms_reports.domain.model.ExecutiveSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExecutiveSnapshotRepository extends JpaRepository<ExecutiveSnapshot, UUID> {
    Optional<ExecutiveSnapshot> findByTenantIdAndPeriod(String tenantId, String period);
    List<ExecutiveSnapshot> findAllByOrderByPeriodDesc();
}
