package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.service.FiberLinkService;
import dd.conexiones.dd_ms_network.domain.exception.InvalidNapConfigurationException;
import dd.conexiones.dd_ms_network.domain.exception.NodeNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.FiberLink;
import dd.conexiones.dd_ms_network.domain.model.Node;
import dd.conexiones.dd_ms_network.domain.model.enums.FiberType;
import dd.conexiones.dd_ms_network.domain.repository.FiberLinkRepository;
import dd.conexiones.dd_ms_network.domain.repository.NodeRepository;
import dd.conexiones.dd_ms_network.web.dto.CreateFiberLinkRequest;
import dd.conexiones.dd_ms_network.web.dto.FiberLinkDTO;
import dd.conexiones.dd_ms_network.web.dto.FiberPointDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateFiberLinkRequest;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FiberLinkServiceImpl implements FiberLinkService {

    private final FiberLinkRepository fiberLinkRepository;
    private final NodeRepository nodeRepository;

    public FiberLinkServiceImpl(FiberLinkRepository fiberLinkRepository, NodeRepository nodeRepository) {
        this.fiberLinkRepository = fiberLinkRepository;
        this.nodeRepository = nodeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiberLinkDTO> findAll() {
        String tenantId = TenantContext.require();
        return fiberLinkRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Audited("fiber_link.create")
    public FiberLinkDTO create(CreateFiberLinkRequest request) {
        String tenantId = TenantContext.require();
        validateEndpoints(tenantId, request.fromNodeId(), request.toNodeId());

        FiberType type = request.fiberType() != null ? request.fiberType() : FiberType.DISTRIBUTION;
        FiberLink link = new FiberLink();
        link.setTenantId(tenantId);
        link.setName(cleanName(request.name()));
        link.setFromNodeId(request.fromNodeId());
        link.setToNodeId(request.toNodeId());
        link.setFiberType(type);
        link.setColor(resolveColor(request.color(), type));
        link.setWaypointsData(encodeWaypoints(request.waypoints()));

        return toDto(fiberLinkRepository.saveAndFlush(link));
    }

    @Override
    @Audited("fiber_link.update")
    public FiberLinkDTO update(UUID id, UpdateFiberLinkRequest request) {
        String tenantId = TenantContext.require();
        FiberLink link = findOwnLink(id, tenantId);
        validateEndpoints(tenantId, request.fromNodeId(), request.toNodeId());

        FiberType type = request.fiberType() != null ? request.fiberType() : link.getFiberType();
        link.setName(cleanName(request.name()));
        link.setFromNodeId(request.fromNodeId());
        link.setToNodeId(request.toNodeId());
        link.setFiberType(type);
        link.setColor(resolveColor(request.color(), type));
        if (request.waypoints() != null) {
            link.setWaypointsData(encodeWaypoints(request.waypoints()));
        }

        return toDto(fiberLinkRepository.saveAndFlush(link));
    }

    @Override
    @Audited("fiber_link.delete")
    public void delete(UUID id) {
        String tenantId = TenantContext.require();
        fiberLinkRepository.delete(findOwnLink(id, tenantId));
    }

    private void validateEndpoints(String tenantId, UUID fromNodeId, UUID toNodeId) {
        if (fromNodeId.equals(toNodeId)) {
            throw new InvalidNapConfigurationException("El nodo de origen y el nodo de destino deben ser diferentes.");
        }
        Node from = nodeRepository.findById(fromNodeId)
                .filter(n -> n.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NodeNotFoundException("Nodo de origen no encontrado: " + fromNodeId));
        Node to = nodeRepository.findById(toNodeId)
                .filter(n -> n.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NodeNotFoundException("Nodo de destino no encontrado: " + toNodeId));
        if (from.getId().equals(to.getId())) {
            throw new InvalidNapConfigurationException("El nodo de origen y destino deben ser distintos.");
        }
    }

    private FiberLink findOwnLink(UUID id, String tenantId) {
        return fiberLinkRepository.findById(id)
                .filter(l -> l.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NodeNotFoundException("Trazado de fibra no encontrado: " + id));
    }

    private String cleanName(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.length() > 120 ? trimmed.substring(0, 120) : trimmed;
    }

    private String resolveColor(String color, FiberType type) {
        if (color != null && color.trim().matches("^#[0-9a-fA-F]{6}$")) {
            return color.trim();
        }
        return switch (type) {
            case TRUNK -> "#7c3aed";
            case DISTRIBUTION -> "#06b6d4";
            case DROP -> "#10b981";
        };
    }

    private String encodeWaypoints(List<FiberPointDTO> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) {
            return null;
        }
        return waypoints.stream()
                .filter(p -> p.lat() >= -90 && p.lat() <= 90 && p.lon() >= -180 && p.lon() <= 180)
                .map(p -> String.format(Locale.US, "%.6f,%.6f", p.lat(), p.lon()))
                .collect(Collectors.joining(";"));
    }

    private List<FiberPointDTO> decodeWaypoints(String data) {
        if (data == null || data.isBlank()) {
            return List.of();
        }
        List<FiberPointDTO> points = new ArrayList<>();
        for (String pair : data.split(";")) {
            String[] parts = pair.split(",");
            if (parts.length == 2) {
                try {
                    points.add(new FiberPointDTO(Double.parseDouble(parts[0]), Double.parseDouble(parts[1])));
                } catch (NumberFormatException ignored) {
                    // ignora coordenada corrupta
                }
            }
        }
        return points;
    }

    private FiberLinkDTO toDto(FiberLink link) {
        return new FiberLinkDTO(
                link.getId(),
                link.getName(),
                link.getFromNodeId(),
                link.getToNodeId(),
                link.getFiberType(),
                link.getColor(),
                decodeWaypoints(link.getWaypointsData()),
                link.getCreatedAt()
        );
    }
}
