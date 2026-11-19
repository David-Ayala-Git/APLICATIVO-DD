package dd.conexiones.dd_ms_crm.domain.exception;

public class ContractNotFoundException extends RuntimeException {
    public ContractNotFoundException(String identifier) {
        super("Contrato no encontrado: " + identifier);
    }
}
