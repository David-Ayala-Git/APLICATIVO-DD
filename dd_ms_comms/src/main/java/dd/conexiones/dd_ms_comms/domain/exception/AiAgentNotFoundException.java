package dd.conexiones.dd_ms_comms.domain.exception;

public class AiAgentNotFoundException extends RuntimeException {
    public AiAgentNotFoundException(String identifier) {
        super("Agente de IA no encontrado: " + identifier);
    }
}
