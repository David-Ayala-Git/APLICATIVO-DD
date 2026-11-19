package dd.conexiones.dd_ms_support.domain.repository;

import dd.conexiones.dd_ms_support.domain.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {
}
