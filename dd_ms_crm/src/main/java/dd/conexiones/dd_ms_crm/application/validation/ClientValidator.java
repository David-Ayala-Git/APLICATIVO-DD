package dd.conexiones.dd_ms_crm.application.validation;

import dd.conexiones.dd_ms_crm.domain.exception.DuplicateClientDocumentException;
import dd.conexiones.dd_ms_crm.domain.repository.ClientRepository;
import org.springframework.stereotype.Component;

@Component
public class ClientValidator {

    private final ClientRepository clientRepository;

    public ClientValidator(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void validateDocumentAvailable(String tenantId, String document) {
        if (clientRepository.existsByTenantIdAndDocument(tenantId, document)) {
            throw new DuplicateClientDocumentException(document);
        }
    }
}
