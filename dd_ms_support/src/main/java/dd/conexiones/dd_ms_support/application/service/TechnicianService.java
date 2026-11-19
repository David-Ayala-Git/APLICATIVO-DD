package dd.conexiones.dd_ms_support.application.service;

import dd.conexiones.dd_ms_support.web.dto.CreateTechnicianRequest;
import dd.conexiones.dd_ms_support.web.dto.TechnicianDTO;
import dd.conexiones.dd_ms_support.web.dto.UpdateTechnicianStatusRequest;

import java.util.List;
import java.util.UUID;

public interface TechnicianService {
    List<TechnicianDTO> findAll();
    TechnicianDTO findById(UUID id);
    TechnicianDTO create(CreateTechnicianRequest request);
    TechnicianDTO updateStatus(UUID id, UpdateTechnicianStatusRequest request);
}
