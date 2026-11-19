package dd.conexiones.dd_ms_network.application.mapper;

import dd.conexiones.dd_ms_network.domain.model.Node;
import dd.conexiones.dd_ms_network.web.dto.NodeDTO;
import org.springframework.stereotype.Component;

@Component
public class NodeMapper {

    public NodeDTO toDto(Node node) {
        return new NodeDTO(node.getId(), node.getName(), node.getType(), node.getLatitude(), node.getLongitude(),
                node.getStatus(), node.getCreatedAt());
    }
}
