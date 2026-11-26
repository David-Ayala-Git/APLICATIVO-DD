package dd.conexiones.dd_ms_network.application.mapper;

import dd.conexiones.dd_ms_network.domain.model.NapPortAssignment;
import dd.conexiones.dd_ms_network.domain.model.Node;
import dd.conexiones.dd_ms_network.domain.model.enums.NodeType;
import dd.conexiones.dd_ms_network.web.dto.NapPortAssignmentDTO;
import dd.conexiones.dd_ms_network.web.dto.NodeDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NodeMapper {

    public NodeDTO toDto(Node node) {
        return toDto(node, List.of());
    }

    public NodeDTO toDto(Node node, List<NapPortAssignment> assignments) {
        List<NapPortAssignmentDTO> assignmentDtos = assignments.stream()
                .map(a -> new NapPortAssignmentDTO(
                        a.getId(),
                        a.getNodeId(),
                        a.getClientId(),
                        a.getPortNumber(),
                        a.getAssignedAt()
                ))
                .toList();

        Integer capacity = node.getType() == NodeType.NAP
                ? (node.getNapCapacity() != null ? node.getNapCapacity() : 8)
                : null;

        int used = assignmentDtos.size();
        int available = capacity != null ? Math.max(0, capacity - used) : 0;
        String napStatus = computeNapStatus(capacity, available);

        return new NodeDTO(
                node.getId(),
                node.getName(),
                node.getType(),
                node.getLatitude(),
                node.getLongitude(),
                node.getStatus(),
                capacity,
                used,
                available,
                napStatus,
                assignmentDtos,
                node.getCreatedAt()
        );
    }

    /**
     * Estado de control de ocupacion para cajas NAP (1x8 o 1x16):
     * - FULL: 0 puertos disponibles
     * - LOW: pocos puertos disponibles (<= 25% libres: 1-2 en 1x8, 1-4 en 1x16)
     * - AVAILABLE: puertos suficientes disponibles
     */
    private String computeNapStatus(Integer capacity, int available) {
        if (capacity == null) {
            return null;
        }
        if (available <= 0) {
            return "FULL";
        }
        if (available * 4 <= capacity) {
            return "LOW";
        }
        return "AVAILABLE";
    }
}
