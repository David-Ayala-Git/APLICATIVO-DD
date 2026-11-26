package dd.conexiones.dd_ms_support.application.mapper;

import dd.conexiones.dd_ms_support.domain.model.WorkOrder;
import dd.conexiones.dd_ms_support.web.dto.WorkOrderDTO;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderMapper {

    public WorkOrderDTO toDto(WorkOrder workOrder) {
        return new WorkOrderDTO(workOrder.getId(), workOrder.getClientId(), workOrder.getTicketId(),
                workOrder.getType(), workOrder.getTechnicianId(), workOrder.getScheduledAt(),
                workOrder.getAddress(), workOrder.getStatus(), workOrder.getStartedAt(),
                workOrder.getFinishedAt(), workOrder.getCreatedAt());
    }
}
