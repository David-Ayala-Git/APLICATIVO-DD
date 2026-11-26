package dd.conexiones.dd_ms_crm.application.validation;

import dd.conexiones.dd_ms_crm.domain.exception.DuplicateClientCodeException;
import dd.conexiones.dd_ms_crm.domain.exception.DuplicateClientDocumentException;
import dd.conexiones.dd_ms_crm.domain.exception.InvalidClientCodeException;
import dd.conexiones.dd_ms_crm.domain.repository.ClientRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

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

    /** Normaliza un codigo manual al formato 0001 (minimo 4 digitos, >= 1). */
    public String normalizeCode(String rawCode) {
        String trimmed = rawCode == null ? "" : rawCode.trim();
        if (!trimmed.matches("^\\d{1,16}$")) {
            throw new InvalidClientCodeException("El codigo del cliente debe contener solo digitos (formato 0001)");
        }
        long num = Long.parseLong(trimmed);
        if (num < 1) {
            throw new InvalidClientCodeException("El codigo del cliente debe empezar desde 0001");
        }
        return String.format("%04d", num);
    }

    public String resolveCodeForCreate(String tenantId, String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return nextAvailableCode(tenantId);
        }
        String code = normalizeCode(rawCode);
        if (clientRepository.existsByTenantIdAndClientCode(tenantId, code)) {
            throw new DuplicateClientCodeException(code);
        }
        return code;
    }

    public String resolveCodeForUpdate(String tenantId, UUID clientId, String rawCode, String currentCode) {
        if (rawCode == null || rawCode.isBlank()) {
            return currentCode;
        }
        String code = normalizeCode(rawCode);
        if (clientRepository.existsByTenantIdAndClientCodeAndIdNot(tenantId, code, clientId)) {
            throw new DuplicateClientCodeException(code);
        }
        return code;
    }

    public String nextAvailableCode(String tenantId) {
        List<String> existing = clientRepository.findClientCodesByTenantId(tenantId);
        long max = 0;
        for (String code : existing) {
            if (code != null && code.matches("^\\d{1,16}$")) {
                long value = Long.parseLong(code);
                if (value > max) {
                    max = value;
                }
            }
        }
        long candidate = max + 1;
        String formatted = String.format("%04d", candidate);
        while (clientRepository.existsByTenantIdAndClientCode(tenantId, formatted)) {
            candidate++;
            formatted = String.format("%04d", candidate);
        }
        return formatted;
    }
}
