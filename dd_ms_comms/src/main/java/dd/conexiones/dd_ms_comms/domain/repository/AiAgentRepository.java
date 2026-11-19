package dd.conexiones.dd_ms_comms.domain.repository;

import dd.conexiones.dd_ms_comms.domain.model.AiAgent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiAgentRepository extends JpaRepository<AiAgent, UUID> {
}
