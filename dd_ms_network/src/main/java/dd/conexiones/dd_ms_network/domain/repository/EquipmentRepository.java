package dd.conexiones.dd_ms_network.domain.repository;

import dd.conexiones.dd_ms_network.domain.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    boolean existsByTenantIdAndSerialNumber(String tenantId, String serialNumber);
}
