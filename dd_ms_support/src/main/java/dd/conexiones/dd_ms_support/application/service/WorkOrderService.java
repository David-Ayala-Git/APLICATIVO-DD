package dd.conexiones.dd_ms_support.application.service;

import dd.conexiones.dd_ms_support.web.dto.CreateWorkOrderRequest;
import dd.conexiones.dd_ms_support.web.dto.WorkOrderDTO;

import java.util.List;
import java.util.UUID;

public interface WorkOrderService {
    List<WorkOrderDTO> findAll();
    WorkOrderDTO findById(UUID id);
    WorkOrderDTO create(CreateWorkOrderRequest request);
    WorkOrderDTO complete(UUID id);
}
