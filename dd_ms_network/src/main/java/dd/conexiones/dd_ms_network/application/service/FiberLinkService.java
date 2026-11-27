package dd.conexiones.dd_ms_network.application.service;

import dd.conexiones.dd_ms_network.web.dto.CreateFiberLinkRequest;
import dd.conexiones.dd_ms_network.web.dto.FiberLinkDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateFiberLinkRequest;

import java.util.List;
import java.util.UUID;

public interface FiberLinkService {
    List<FiberLinkDTO> findAll();
    FiberLinkDTO create(CreateFiberLinkRequest request);
    FiberLinkDTO update(UUID id, UpdateFiberLinkRequest request);
    void delete(UUID id);
}
