package dd.conexiones.dd_ms_network.application.validation;

import dd.conexiones.dd_ms_network.domain.exception.DuplicateSerialException;
import dd.conexiones.dd_ms_network.domain.repository.EquipmentRepository;
import org.springframework.stereotype.Component;

@Component
public class EquipmentValidator {

    private final EquipmentRepository equipmentRepository;

    public EquipmentValidator(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public void validateSerialAvailable(String tenantId, String serialNumber) {
        if (equipmentRepository.existsByTenantIdAndSerialNumber(tenantId, serialNumber)) {
            throw new DuplicateSerialException(serialNumber);
        }
    }
}
