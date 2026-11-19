package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.mapper.EquipmentMapper;
import dd.conexiones.dd_ms_network.application.service.EquipmentService;
import dd.conexiones.dd_ms_network.application.validation.EquipmentValidator;
import dd.conexiones.dd_ms_network.domain.exception.EquipmentNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.Equipment;
import dd.conexiones.dd_ms_network.domain.model.enums.EquipmentStatus;
import dd.conexiones.dd_ms_network.domain.repository.EquipmentRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_network.web.dto.AssignEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.CreateEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.EquipmentDTO;
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
    public EquipmentDTO assign(UUID id, AssignEquipmentRequest request) {
        tenantFilterEnabler.enable(entityManager);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id.toString()));

        equipment.setAssignedClientId(request.clientId());
        equipment.setStatus(EquipmentStatus.ASSIGNED);

        return equipmentMapper.toDto(equipmentRepository.save(equipment));
    }
}
