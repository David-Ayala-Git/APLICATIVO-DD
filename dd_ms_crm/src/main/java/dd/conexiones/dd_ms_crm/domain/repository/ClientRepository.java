package dd.conexiones.dd_ms_crm.domain.repository;

import dd.conexiones.dd_ms_crm.domain.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByTenantIdAndDocument(String tenantId, String document);
}
