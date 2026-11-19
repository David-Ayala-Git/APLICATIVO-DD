package dd.conexiones.dd_ms_network.application.service;

import dd.conexiones.dd_ms_network.web.dto.CreateNodeRequest;
import dd.conexiones.dd_ms_network.web.dto.NodeDTO;

import java.util.List;
import java.util.UUID;

public interface NodeService {
    List<NodeDTO> findAll();
    NodeDTO findById(UUID id);
    NodeDTO create(CreateNodeRequest request);
}
