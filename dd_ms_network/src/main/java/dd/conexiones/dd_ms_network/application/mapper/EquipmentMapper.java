package dd.conexiones.dd_ms_network.application.mapper;

import dd.conexiones.dd_ms_network.domain.model.Equipment;
import dd.conexiones.dd_ms_network.web.dto.EquipmentDTO;
import org.springframework.stereotype.Component;

@Component
public class EquipmentMapper {

    public EquipmentDTO toDto(Equipment equipment) {
        return new EquipmentDTO(equipment.getId(), equipment.getType(), equipment.getBrand(), equipment.getModel(),
                equipment.getSerialNumber(), equipment.getStatus(), equipment.getAssignedClientId(),
                equipment.getCreatedAt());
    }
}
