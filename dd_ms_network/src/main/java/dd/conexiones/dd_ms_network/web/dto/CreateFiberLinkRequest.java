package dd.conexiones.dd_ms_network.web.dto;

import dd.conexiones.dd_ms_network.domain.model.enums.FiberType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateFiberLinkRequest(
        String name,
        @NotNull UUID fromNodeId,
        @NotNull UUID toNodeId,
        FiberType fiberType,
        String color,
        List<FiberPointDTO> waypoints
) {
}
