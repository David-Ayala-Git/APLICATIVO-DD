package dd.conexiones.dd_ms_crm.application.service;

import dd.conexiones.dd_ms_crm.web.dto.CreatePlanRequest;
import dd.conexiones.dd_ms_crm.web.dto.PlanDTO;

import java.util.List;
import java.util.UUID;

public interface PlanService {
    List<PlanDTO> findAll();
    PlanDTO findById(UUID id);
    PlanDTO create(CreatePlanRequest request);
}
