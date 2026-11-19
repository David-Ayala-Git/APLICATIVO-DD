package dd.conexiones.dd_ms_comms.domain.repository;

import dd.conexiones.dd_ms_comms.domain.model.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AiMessageRepository extends JpaRepository<AiMessage, UUID> {
    List<AiMessage> findByAgentIdAndSessionIdOrderByCreatedAtAsc(UUID agentId, String sessionId);
}
