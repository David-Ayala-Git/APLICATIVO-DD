package dd.conexiones.dd_ms_crm.application.impl;

import dd.conexiones.dd_ms_crm.application.mapper.PlanMapper;
import dd.conexiones.dd_ms_crm.application.service.PlanService;
import dd.conexiones.dd_ms_crm.domain.exception.PlanNotFoundException;
import dd.conexiones.dd_ms_crm.domain.model.Plan;
import dd.conexiones.dd_ms_crm.domain.repository.PlanRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_crm.web.dto.CreatePlanRequest;
import dd.conexiones.dd_ms_crm.web.dto.PlanDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public PlanServiceImpl(PlanRepository planRepository, PlanMapper planMapper, TenantFilterEnabler tenantFilterEnabler) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return planRepository.findAll().stream().map(planMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return planRepository.findById(id).map(planMapper::toDto)
                .orElseThrow(() -> new PlanNotFoundException(id.toString()));
    }

    @Override
    public PlanDTO create(CreatePlanRequest request) {
        Plan plan = new Plan();
        plan.setTenantId(TenantContext.require());
        plan.setName(request.name());
        plan.setDownMbps(request.downMbps());
        plan.setUpMbps(request.upMbps());
        plan.setTechnology(request.technology());
        plan.setPrice(request.price());

        return planMapper.toDto(planRepository.saveAndFlush(plan));
    }
}
