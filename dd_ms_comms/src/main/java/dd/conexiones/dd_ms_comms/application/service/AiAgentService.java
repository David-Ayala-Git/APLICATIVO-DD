package dd.conexiones.dd_ms_comms.application.service;

import dd.conexiones.dd_ms_comms.web.dto.AiAgentDTO;
import dd.conexiones.dd_ms_comms.web.dto.ChatRequest;
import dd.conexiones.dd_ms_comms.web.dto.ChatResponse;
import dd.conexiones.dd_ms_comms.web.dto.CreateAiAgentRequest;

import java.util.List;
import java.util.UUID;

public interface AiAgentService {
    List<AiAgentDTO> findAll();
    AiAgentDTO findById(UUID id);
    AiAgentDTO create(CreateAiAgentRequest request);
    ChatResponse chat(UUID agentId, ChatRequest request);
}
