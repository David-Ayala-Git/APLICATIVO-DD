package dd.conexiones.dd_ms_crm.domain.repository;

import dd.conexiones.dd_ms_crm.domain.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContractRepository extends JpaRepository<Contract, UUID> {
}
