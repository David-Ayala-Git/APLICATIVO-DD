package dd.conexiones.dd_ms_comms.application.mapper;

import dd.conexiones.dd_ms_comms.domain.model.AiAgent;
import dd.conexiones.dd_ms_comms.web.dto.AiAgentDTO;
import org.springframework.stereotype.Component;

@Component
public class AiAgentMapper {

    public AiAgentDTO toDto(AiAgent agent) {
        return new AiAgentDTO(agent.getId(), agent.getName(), agent.getChannel(), agent.getSystemPrompt(),
                agent.getTools(), agent.isEnabled(), agent.getCreatedAt());
    }
}
