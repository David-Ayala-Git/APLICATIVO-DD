package dd.conexiones.dd_ms_comms.web.controller;

import dd.conexiones.dd_ms_comms.application.service.AiAgentService;
import dd.conexiones.dd_ms_comms.web.dto.AiAgentDTO;
import dd.conexiones.dd_ms_comms.web.dto.ChatRequest;
import dd.conexiones.dd_ms_comms.web.dto.ChatResponse;
import dd.conexiones.dd_ms_comms.web.dto.CreateAiAgentRequest;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai/agents")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @GetMapping
    public List<AiAgentDTO> findAll() {
        return aiAgentService.findAll();
    }

    @GetMapping("/{id}")
    public AiAgentDTO findById(@PathVariable UUID id) {
        return aiAgentService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AiAgentDTO create(@Valid @RequestBody CreateAiAgentRequest request) {
        return aiAgentService.create(request);
    }

    @PostMapping("/{id}/chat")
    public ChatResponse chat(@PathVariable UUID id, @Valid @RequestBody ChatRequest request) {
        return aiAgentService.chat(id, request);
    }
}
