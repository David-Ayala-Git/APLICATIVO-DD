package dd.conexiones.dd_ms_comms.application.impl;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.ToolUseBlockParam;
import dd.conexiones.dd_ms_comms.application.mapper.AiAgentMapper;
import dd.conexiones.dd_ms_comms.application.service.AiAgentService;
import dd.conexiones.dd_ms_comms.domain.exception.AgentDisabledException;
import dd.conexiones.dd_ms_comms.domain.exception.AiAgentNotFoundException;
import dd.conexiones.dd_ms_comms.domain.model.AiAgent;
import dd.conexiones.dd_ms_comms.domain.model.AiMessage;
import dd.conexiones.dd_ms_comms.domain.model.enums.ChatRole;
import dd.conexiones.dd_ms_comms.domain.repository.AiAgentRepository;
import dd.conexiones.dd_ms_comms.domain.repository.AiMessageRepository;
import dd.conexiones.dd_ms_comms.infrastructure.client.AiToolDefinitions;
import dd.conexiones.dd_ms_comms.infrastructure.client.ToolExecutor;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_comms.web.dto.AiAgentDTO;
import dd.conexiones.dd_ms_comms.web.dto.ChatRequest;
import dd.conexiones.dd_ms_comms.web.dto.ChatResponse;
import dd.conexiones.dd_ms_comms.web.dto.CreateAiAgentRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orquesta la conversacion con un AiAgent: llama a la API de Anthropic con el
 * system prompt y las tools del agente, y cuando el modelo pide usar una tool
 * la ejecuta via {@link ToolExecutor} contra el microservicio dueno del dato
 * (nunca acceso directo a base de datos desde el modelo).
 */
@Service
@Transactional
public class AiAgentServiceImpl implements AiAgentService {

    private static final String MODEL = "claude-opus-5";
    private static final int MAX_TOOL_ITERATIONS = 5;

    private final AiAgentRepository aiAgentRepository;
    private final AiMessageRepository aiMessageRepository;
    private final AiAgentMapper aiAgentMapper;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final AnthropicClient anthropicClient;
    private final AiToolDefinitions aiToolDefinitions;
    private final ToolExecutor toolExecutor;

    @PersistenceContext
    private EntityManager entityManager;

    public AiAgentServiceImpl(AiAgentRepository aiAgentRepository, AiMessageRepository aiMessageRepository,
                               AiAgentMapper aiAgentMapper, TenantFilterEnabler tenantFilterEnabler,
                               AnthropicClient anthropicClient, AiToolDefinitions aiToolDefinitions,
                               ToolExecutor toolExecutor) {
        this.aiAgentRepository = aiAgentRepository;
        this.aiMessageRepository = aiMessageRepository;
        this.aiAgentMapper = aiAgentMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.anthropicClient = anthropicClient;
        this.aiToolDefinitions = aiToolDefinitions;
        this.toolExecutor = toolExecutor;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiAgentDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return aiAgentRepository.findAll().stream().map(aiAgentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AiAgentDTO findById(UUID id) {
        return aiAgentMapper.toDto(getAgentOrThrow(id));
    }

    @Override
    public AiAgentDTO create(CreateAiAgentRequest request) {
        AiAgent agent = new AiAgent();
        agent.setTenantId(TenantContext.require());
        agent.setName(request.name());
        agent.setChannel(request.channel());
        agent.setSystemPrompt(request.systemPrompt());
        agent.setTools(request.tools());

        return aiAgentMapper.toDto(aiAgentRepository.saveAndFlush(agent));
    }

    @Override
    @RequiresModule("comms")
    public ChatResponse chat(UUID agentId, ChatRequest request) {
        String tenantId = TenantContext.require();
        AiAgent agent = getAgentOrThrow(agentId);
        if (!agent.isEnabled()) {
            throw new AgentDisabledException(agentId.toString());
        }

        List<AiMessage> history = aiMessageRepository
                .findByAgentIdAndSessionIdOrderByCreatedAtAsc(agentId, request.sessionId());

        List<MessageParam> messages = new ArrayList<>();
        for (AiMessage entry : history) {
            MessageParam.Role role = entry.getRole() == ChatRole.USER
                    ? MessageParam.Role.USER
                    : MessageParam.Role.ASSISTANT;
            messages.add(MessageParam.builder().role(role).content(entry.getContent()).build());
        }
        messages.add(MessageParam.builder().role(MessageParam.Role.USER).content(request.message()).build());

        String replyText = runConversation(agent, messages, tenantId);

        persistTurn(tenantId, agentId, request.sessionId(), ChatRole.USER, request.message());
        persistTurn(tenantId, agentId, request.sessionId(), ChatRole.ASSISTANT, replyText);

        return new ChatResponse(request.sessionId(), replyText);
    }

    private String runConversation(AiAgent agent, List<MessageParam> messages, String tenantId) {
        List<Tool> tools = aiToolDefinitions.all();

        for (int i = 0; i < MAX_TOOL_ITERATIONS; i++) {
            MessageCreateParams.Builder builder = MessageCreateParams.builder()
                    .model(MODEL)
                    .maxTokens(4096L)
                    .system(agent.getSystemPrompt())
                    .messages(messages);
            for (Tool tool : tools) {
                builder.addTool(tool);
            }

            Message response = anthropicClient.messages().create(builder.build());

            boolean usedTool = response.stopReason().isPresent()
                    && response.stopReason().get() == StopReason.TOOL_USE;

            if (usedTool) {
                List<ContentBlockParam> assistantBlocks = new ArrayList<>();
                List<ContentBlockParam> toolResults = new ArrayList<>();

                for (ContentBlock block : response.content()) {
                    block.text().ifPresent(t ->
                            assistantBlocks.add(ContentBlockParam.ofText(
                                    TextBlockParam.builder().text(t.text()).build())));
                    block.toolUse().ifPresent(tu -> {
                        assistantBlocks.add(ContentBlockParam.ofToolUse(ToolUseBlockParam.builder()
                                .id(tu.id())
                                .name(tu.name())
                                .input(tu._input())
                                .build()));
                        String result = executeTool(tenantId, tu);
                        toolResults.add(ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                                .toolUseId(tu.id())
                                .content(result)
                                .build()));
                    });
                }

                messages.add(MessageParam.builder().role(MessageParam.Role.ASSISTANT)
                        .contentOfBlockParams(assistantBlocks).build());
                messages.add(MessageParam.builder().role(MessageParam.Role.USER)
                        .contentOfBlockParams(toolResults).build());
                continue;
            }

            return response.content().stream()
                    .flatMap(b -> b.text().stream())
                    .map(t -> t.text())
                    .reduce("", (a, b) -> a + b);
        }

        return "No se pudo completar la conversacion (demasiadas herramientas encadenadas).";
    }

    @SuppressWarnings("unchecked")
    private String executeTool(String tenantId, ToolUseBlock toolUse) {
        Map<String, Object> input = toolUse._input().convert(Map.class);
        try {
            return switch (toolUse.name()) {
                case "consultar_cliente" ->
                        toolExecutor.consultarCliente(tenantId, String.valueOf(input.get("clientId")));
                case "crear_ticket" -> toolExecutor.crearTicket(tenantId, String.valueOf(input.get("clientId")),
                        String.valueOf(input.get("reason")), String.valueOf(input.get("priority")));
                case "estado_factura" ->
                        toolExecutor.estadoFactura(tenantId, String.valueOf(input.get("invoiceId")));
                default -> "{\"error\": \"tool desconocida: " + toolUse.name() + "\"}";
            };
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private void persistTurn(String tenantId, UUID agentId, String sessionId, ChatRole role, String content) {
        AiMessage message = new AiMessage();
        message.setTenantId(tenantId);
        message.setAgentId(agentId);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        aiMessageRepository.save(message);
    }

    private AiAgent getAgentOrThrow(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return aiAgentRepository.findById(id).orElseThrow(() -> new AiAgentNotFoundException(id.toString()));
    }
}
