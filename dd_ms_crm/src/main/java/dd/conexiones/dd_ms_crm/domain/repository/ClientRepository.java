package dd.conexiones.dd_ms_crm.domain.repository;

import dd.conexiones.dd_ms_crm.domain.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByTenantIdAndDocument(String tenantId, String document);

    boolean existsByTenantIdAndClientCode(String tenantId, String clientCode);

    boolean existsByTenantIdAndClientCodeAndIdNot(String tenantId, String clientCode, UUID id);

    boolean existsByTenantIdAndPlanId(String tenantId, UUID planId);

    @Query("select c.clientCode from Client c where c.tenantId = :tenantId")
    List<String> findClientCodesByTenantId(@Param("tenantId") String tenantId);
}
