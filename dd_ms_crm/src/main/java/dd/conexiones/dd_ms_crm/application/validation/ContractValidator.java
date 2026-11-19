package dd.conexiones.dd_ms_crm.application.validation;

import dd.conexiones.dd_ms_crm.domain.exception.ClientNotFoundException;
import dd.conexiones.dd_ms_crm.domain.exception.PlanNotFoundException;
import dd.conexiones.dd_ms_crm.domain.repository.ClientRepository;
import dd.conexiones.dd_ms_crm.domain.repository.PlanRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ContractValidator {

    private final ClientRepository clientRepository;
    private final PlanRepository planRepository;

    public ContractValidator(ClientRepository clientRepository, PlanRepository planRepository) {
        this.clientRepository = clientRepository;
        this.planRepository = planRepository;
    }

    public void validateClientExists(UUID clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ClientNotFoundException(clientId.toString());
        }
    }

    public void validatePlanExists(UUID planId) {
        if (!planRepository.existsById(planId)) {
            throw new PlanNotFoundException(planId.toString());
        }
    }
}
