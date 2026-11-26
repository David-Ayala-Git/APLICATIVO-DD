package dd.conexiones.dd_ms_network.application.service;

import dd.conexiones.dd_ms_network.web.dto.AssignEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.CreateEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.EquipmentDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateEquipmentRequest;

import java.util.List;
import java.util.UUID;

public interface EquipmentService {
    List<EquipmentDTO> findAll();
    EquipmentDTO findById(UUID id);
    EquipmentDTO create(CreateEquipmentRequest request);
    EquipmentDTO update(UUID id, UpdateEquipmentRequest request);
    void delete(UUID id);
    EquipmentDTO assign(UUID id, AssignEquipmentRequest request);
}
