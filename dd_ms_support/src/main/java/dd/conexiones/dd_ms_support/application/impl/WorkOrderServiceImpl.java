package dd.conexiones.dd_ms_support.application.impl;

import dd.conexiones.dd_ms_support.application.mapper.WorkOrderMapper;
import dd.conexiones.dd_ms_support.application.service.WorkOrderService;
import dd.conexiones.dd_ms_support.domain.exception.WorkOrderNotFoundException;
import dd.conexiones.dd_ms_support.domain.model.WorkOrder;
import dd.conexiones.dd_ms_support.domain.model.enums.WorkOrderStatus;
import dd.conexiones.dd_ms_support.domain.repository.WorkOrderRepository;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_support.web.dto.CreateWorkOrderRequest;
import dd.conexiones.dd_ms_support.web.dto.WorkOrderDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMapper workOrderMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public WorkOrderServiceImpl(WorkOrderRepository workOrderRepository, WorkOrderMapper workOrderMapper,
                                 TenantFilterEnabler tenantFilterEnabler) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderMapper = workOrderMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkOrderDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return workOrderRepository.findAll().stream().map(workOrderMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOrderDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return workOrderRepository.findById(id).map(workOrderMapper::toDto)
                .orElseThrow(() -> new WorkOrderNotFoundException(id.toString()));
    }

    @Override
    public WorkOrderDTO create(CreateWorkOrderRequest request) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setTenantId(TenantContext.require());
        workOrder.setClientId(request.clientId());
        workOrder.setTicketId(request.ticketId());
        workOrder.setType(request.type());
        workOrder.setTechnicianId(request.technicianId());
        workOrder.setScheduledAt(request.scheduledAt());
        workOrder.setAddress(request.address());

        return workOrderMapper.toDto(workOrderRepository.saveAndFlush(workOrder));
    }

    @Override
    public WorkOrderDTO complete(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new WorkOrderNotFoundException(id.toString()));

        workOrder.setStatus(WorkOrderStatus.COMPLETED);
        return workOrderMapper.toDto(workOrderRepository.save(workOrder));
    }
}
