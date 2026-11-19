package dd.conexiones.dd_ms_support.application.impl;

import dd.conexiones.dd_ms_support.application.mapper.TechnicianMapper;
import dd.conexiones.dd_ms_support.application.service.TechnicianService;
import dd.conexiones.dd_ms_support.domain.exception.TechnicianNotFoundException;
import dd.conexiones.dd_ms_support.domain.model.Technician;
import dd.conexiones.dd_ms_support.domain.repository.TechnicianRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_support.web.dto.CreateTechnicianRequest;
import dd.conexiones.dd_ms_support.web.dto.TechnicianDTO;
import dd.conexiones.dd_ms_support.web.dto.UpdateTechnicianStatusRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TechnicianServiceImpl implements TechnicianService {

    private final TechnicianRepository technicianRepository;
    private final TechnicianMapper technicianMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public TechnicianServiceImpl(TechnicianRepository technicianRepository, TechnicianMapper technicianMapper,
                                  TenantFilterEnabler tenantFilterEnabler) {
        this.technicianRepository = technicianRepository;
        this.technicianMapper = technicianMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicianDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return technicianRepository.findAll().stream().map(technicianMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TechnicianDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return technicianRepository.findById(id).map(technicianMapper::toDto)
                .orElseThrow(() -> new TechnicianNotFoundException(id.toString()));
    }

    @Override
    public TechnicianDTO create(CreateTechnicianRequest request) {
        Technician technician = new Technician();
        technician.setTenantId(TenantContext.require());
        technician.setFullName(request.fullName());
        technician.setPhone(request.phone());

        return technicianMapper.toDto(technicianRepository.saveAndFlush(technician));
    }

    @Override
    public TechnicianDTO updateStatus(UUID id, UpdateTechnicianStatusRequest request) {
        tenantFilterEnabler.enable(entityManager);
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new TechnicianNotFoundException(id.toString()));

        technician.setStatus(request.status());
        return technicianMapper.toDto(technicianRepository.save(technician));
    }
}
