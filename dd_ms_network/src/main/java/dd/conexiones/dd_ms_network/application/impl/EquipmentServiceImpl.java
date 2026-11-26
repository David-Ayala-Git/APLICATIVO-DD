package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.mapper.EquipmentMapper;
import dd.conexiones.dd_ms_network.application.service.EquipmentService;
import dd.conexiones.dd_ms_network.application.validation.EquipmentValidator;
import dd.conexiones.dd_ms_network.domain.exception.EquipmentNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.Equipment;
import dd.conexiones.dd_ms_network.domain.model.enums.EquipmentStatus;
import dd.conexiones.dd_ms_network.domain.repository.EquipmentRepository;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_network.web.dto.AssignEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.CreateEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.EquipmentDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateEquipmentRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentValidator equipmentValidator;
    private final EquipmentMapper equipmentMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository, EquipmentValidator equipmentValidator,
                                 EquipmentMapper equipmentMapper, TenantFilterEnabler tenantFilterEnabler) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentValidator = equipmentValidator;
        this.equipmentMapper = equipmentMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return equipmentRepository.findAll().stream().map(equipmentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return equipmentRepository.findById(id).map(equipmentMapper::toDto)
                .orElseThrow(() -> new EquipmentNotFoundException(id.toString()));
    }

    @Override
    public EquipmentDTO create(CreateEquipmentRequest request) {
        String tenantId = TenantContext.require();
        equipmentValidator.validateSerialAvailable(tenantId, request.serialNumber());

        Equipment equipment = new Equipment();
        equipment.setTenantId(tenantId);
        equipment.setType(request.type());
        equipment.setBrand(request.brand());
        equipment.setModel(request.model());
        equipment.setSerialNumber(request.serialNumber());
        equipment.setStatus(EquipmentStatus.IN_STOCK);

        return equipmentMapper.toDto(equipmentRepository.saveAndFlush(equipment));
    }

    @Override
    @Audited("equipment.update")
    public EquipmentDTO update(UUID id, UpdateEquipmentRequest request) {
        String tenantId = TenantContext.require();
        Equipment equipment = findOwnEquipment(id);
        equipmentValidator.validateSerialAvailableForUpdate(tenantId, request.serialNumber(), id);

        equipment.setType(request.type());
        equipment.setBrand(request.brand());
        equipment.setModel(request.model());
        equipment.setSerialNumber(request.serialNumber());
        if (request.status() != null) {
            equipment.setStatus(request.status());
            if (request.status() != EquipmentStatus.ASSIGNED) {
                equipment.setAssignedClientId(null);
            }
        }
        if (request.assignedClientId() != null) {
            equipment.setAssignedClientId(request.assignedClientId());
            if (request.status() == null) {
                equipment.setStatus(EquipmentStatus.ASSIGNED);
            }
        }
        return equipmentMapper.toDto(equipmentRepository.save(equipment));
    }

    @Override
    @Audited("equipment.delete")
    public void delete(UUID id) {
        equipmentRepository.delete(findOwnEquipment(id));
    }

    @Override
    public EquipmentDTO assign(UUID id, AssignEquipmentRequest request) {
        Equipment equipment = findOwnEquipment(id);

        equipment.setAssignedClientId(request.clientId());
        equipment.setStatus(EquipmentStatus.ASSIGNED);

        return equipmentMapper.toDto(equipmentRepository.save(equipment));
    }

    private Equipment findOwnEquipment(UUID id) {
        String tenantId = TenantContext.require();
        return equipmentRepository.findById(id)
                .filter(e -> e.getTenantId().equals(tenantId))
                .orElseThrow(() -> new EquipmentNotFoundException(id.toString()));
    }
}
