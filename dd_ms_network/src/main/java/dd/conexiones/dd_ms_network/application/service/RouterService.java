package dd.conexiones.dd_ms_network.application.service;

import dd.conexiones.dd_ms_network.web.dto.CreateRouterRequest;
import dd.conexiones.dd_ms_network.web.dto.CutClientRequest;
import dd.conexiones.dd_ms_network.web.dto.QueueLimitRequest;
import dd.conexiones.dd_ms_network.web.dto.RouterDTO;

import java.util.List;
import java.util.UUID;

public interface RouterService {
    List<RouterDTO> findAll();
    RouterDTO findById(UUID id);
    RouterDTO create(CreateRouterRequest request);
    void cutClient(UUID routerId, CutClientRequest request);
    void enableClient(UUID routerId, CutClientRequest request);
    void setQueueLimit(UUID routerId, QueueLimitRequest request);
}
