package dd.conexiones.dd_ms_network.application.validation;

import dd.conexiones.dd_ms_network.domain.exception.DuplicateSerialException;
import dd.conexiones.dd_ms_network.domain.repository.OnuRepository;
import org.springframework.stereotype.Component;

@Component
public class OnuValidator {

    private final OnuRepository onuRepository;

    public OnuValidator(OnuRepository onuRepository) {
        this.onuRepository = onuRepository;
    }

    public void validateSerialAvailable(String tenantId, String serial) {
        if (onuRepository.existsByTenantIdAndSerial(tenantId, serial)) {
            throw new DuplicateSerialException(serial);
        }
    }
}
