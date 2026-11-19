package dd.conexiones.dd_ms_comms.domain.exception;

public class AgentDisabledException extends RuntimeException {
    public AgentDisabledException(String agentId) {
        super("El agente esta deshabilitado: " + agentId);
    }
}
