package dd.conexiones.dd_ms_network.domain.repository;

import dd.conexiones.dd_ms_network.domain.model.Onu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OnuRepository extends JpaRepository<Onu, UUID> {
    boolean existsByTenantIdAndSerial(String tenantId, String serial);
}
