package dd.conexiones.dd_ms_crm.application.service;

import dd.conexiones.dd_ms_crm.web.dto.CreatePlanRequest;
import dd.conexiones.dd_ms_crm.web.dto.PlanDTO;
import dd.conexiones.dd_ms_crm.web.dto.UpdatePlanRequest;

import java.util.List;
import java.util.UUID;

public interface PlanService {
    List<PlanDTO> findAll();
    PlanDTO findById(UUID id);
    PlanDTO create(CreatePlanRequest request);
    PlanDTO update(UUID id, UpdatePlanRequest request);
    PlanDTO updateStatus(UUID id, boolean enabled);
    void delete(UUID id);
}
