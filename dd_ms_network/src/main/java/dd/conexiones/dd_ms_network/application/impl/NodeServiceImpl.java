package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.mapper.NodeMapper;
import dd.conexiones.dd_ms_network.application.service.NodeService;
import dd.conexiones.dd_ms_network.domain.exception.InvalidNapConfigurationException;
import dd.conexiones.dd_ms_network.domain.exception.NapCapacityException;
import dd.conexiones.dd_ms_network.domain.exception.NodeNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.NapPortAssignment;
import dd.conexiones.dd_ms_network.domain.model.Node;
import dd.conexiones.dd_ms_network.domain.model.enums.NodeType;
import dd.conexiones.dd_ms_network.domain.repository.NapPortAssignmentRepository;
import dd.conexiones.dd_ms_network.domain.repository.NodeRepository;
import dd.conexiones.dd_ms_network.web.dto.AssignNapClientRequest;
import dd.conexiones.dd_ms_network.web.dto.CreateNodeRequest;
import dd.conexiones.dd_ms_network.web.dto.NodeDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateNodeRequest;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final NapPortAssignmentRepository assignmentRepository;
    private final NodeMapper nodeMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public NodeServiceImpl(
            NodeRepository nodeRepository,
            NapPortAssignmentRepository assignmentRepository,
            NodeMapper nodeMapper,
            TenantFilterEnabler tenantFilterEnabler
    ) {
        this.nodeRepository = nodeRepository;
        this.assignmentRepository = assignmentRepository;
        this.nodeMapper = nodeMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeDTO> findAll() {
        String tenantId = TenantContext.require();
        tenantFilterEnabler.enable(entityManager);
        Map<UUID, List<NapPortAssignment>> byNode = assignmentRepository
                .findByTenantIdOrderByPortNumberAsc(tenantId)
                .stream()
                .collect(Collectors.groupingBy(NapPortAssignment::getNodeId));

        return nodeRepository.findAll().stream()
                .map(node -> nodeMapper.toDto(node, byNode.getOrDefault(node.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NodeDTO findById(UUID id) {
        String tenantId = TenantContext.require();
        tenantFilterEnabler.enable(entityManager);
        Node node = nodeRepository.findById(id)
                .orElseThrow(() -> new NodeNotFoundException(id.toString()));
        List<NapPortAssignment> assignments = assignmentRepository.findByNodeIdAndTenantIdOrderByPortNumberAsc(id, tenantId);
        return nodeMapper.toDto(node, assignments);
    }

    @Override
    public NodeDTO create(CreateNodeRequest request) {
        Node node = new Node();
        node.setTenantId(TenantContext.require());
        node.setName(request.name());
        node.setType(request.type());
        node.setLatitude(request.latitude());
        node.setLongitude(request.longitude());
        node.setNapCapacity(resolveNapCapacity(request.type(), request.napCapacity()));

        return nodeMapper.toDto(nodeRepository.saveAndFlush(node), List.of());
    }

    @Override
    @Audited("node.update")
    public NodeDTO update(UUID id, UpdateNodeRequest request) {
        String tenantId = TenantContext.require();
        Node node = findOwnNode(id);
        List<NapPortAssignment> existing = assignmentRepository.findByNodeIdAndTenantIdOrderByPortNumberAsc(id, tenantId);

        if (request.type() != NodeType.NAP && !existing.isEmpty()) {
            throw new NapCapacityException(
                    "No puedes cambiar el tipo de esta caja NAP mientras tenga " + existing.size() + " cliente(s) asignado(s)."
            );
        }

        Integer nextCapacity = resolveNapCapacity(
                request.type(),
                request.napCapacity() != null ? request.napCapacity() : node.getNapCapacity()
        );
        if (nextCapacity != null) {
            boolean hasPortAboveLimit = existing.stream().anyMatch(a -> a.getPortNumber() > nextCapacity);
            if (existing.size() > nextCapacity || hasPortAboveLimit) {
                throw new NapCapacityException(
                        "No se puede reducir la caja NAP a 1x" + nextCapacity
                                + " porque tiene puertos ocupados por encima de esa capacidad."
                );
            }
        }

        node.setName(request.name());
        node.setType(request.type());
        node.setLatitude(request.latitude());
        node.setLongitude(request.longitude());
        node.setNapCapacity(nextCapacity);
        if (request.status() != null) {
            node.setStatus(request.status());
        }
        return nodeMapper.toDto(nodeRepository.save(node), existing);
    }

    @Override
    @Audited("node.delete")
    public void delete(UUID id) {
        nodeRepository.delete(findOwnNode(id));
    }

    @Override
    @Audited("nap.assign")
    public NodeDTO assignClient(UUID nodeId, AssignNapClientRequest request) {
        String tenantId = TenantContext.require();
        Node node = findOwnNode(nodeId);
        if (node.getType() != NodeType.NAP) {
            throw new InvalidNapConfigurationException("Solo se pueden asignar clientes a nodos de tipo Caja NAP.");
        }

        int capacity = node.getNapCapacity() != null ? node.getNapCapacity() : 8;
        List<NapPortAssignment> current = assignmentRepository.findByNodeIdAndTenantIdOrderByPortNumberAsc(nodeId, tenantId);

        if (current.size() >= capacity) {
            throw new NapCapacityException(
                    "La caja NAP \"" + node.getName() + "\" esta llena (" + capacity + "/" + capacity + " puertos ocupados)."
            );
        }

        assignmentRepository.findByTenantIdAndClientId(tenantId, request.clientId()).ifPresent(existing -> {
            String otherNodeName = nodeRepository.findById(existing.getNodeId())
                    .map(Node::getName)
                    .orElse("otra caja NAP");
            throw new NapCapacityException(
                    "Este cliente ya esta asignado al puerto #" + existing.getPortNumber()
                            + " de la caja NAP \"" + otherNodeName + "\". Liberalo primero para reasignarlo."
            );
        });

        Set<Integer> usedPorts = current.stream()
                .map(NapPortAssignment::getPortNumber)
                .collect(Collectors.toSet());

        int portNumber;
        if (request.portNumber() != null) {
            portNumber = request.portNumber();
            if (portNumber < 1 || portNumber > capacity) {
                throw new InvalidNapConfigurationException(
                        "El numero de puerto debe estar entre 1 y " + capacity + " para esta caja NAP."
                );
            }
            if (usedPorts.contains(portNumber)) {
                throw new NapCapacityException("El puerto #" + portNumber + " ya esta ocupado.");
            }
        } else {
            portNumber = -1;
            for (int p = 1; p <= capacity; p++) {
                if (!usedPorts.contains(p)) {
                    portNumber = p;
                    break;
                }
            }
            if (portNumber < 0) {
                throw new NapCapacityException("No hay puertos disponibles en esta caja NAP.");
            }
        }

        NapPortAssignment assignment = new NapPortAssignment();
        assignment.setTenantId(tenantId);
        assignment.setNodeId(nodeId);
        assignment.setClientId(request.clientId());
        assignment.setPortNumber(portNumber);
        assignmentRepository.saveAndFlush(assignment);

        List<NapPortAssignment> updated = assignmentRepository.findByNodeIdAndTenantIdOrderByPortNumberAsc(nodeId, tenantId);
        return nodeMapper.toDto(node, updated);
    }

    @Override
    @Audited("nap.unassign")
    public NodeDTO unassignClient(UUID nodeId, UUID assignmentId) {
        String tenantId = TenantContext.require();
        Node node = findOwnNode(nodeId);
        NapPortAssignment assignment = assignmentRepository.findByIdAndNodeIdAndTenantId(assignmentId, nodeId, tenantId)
                .orElseThrow(() -> new NodeNotFoundException("Asignacion de puerto no encontrada: " + assignmentId));

        assignmentRepository.delete(assignment);
        assignmentRepository.flush();

        List<NapPortAssignment> updated = assignmentRepository.findByNodeIdAndTenantIdOrderByPortNumberAsc(nodeId, tenantId);
        return nodeMapper.toDto(node, updated);
    }

    private Integer resolveNapCapacity(NodeType type, Integer requested) {
        if (type != NodeType.NAP) {
            return null;
        }
        int capacity = requested != null ? requested : 8;
        if (capacity != 8 && capacity != 16) {
            throw new InvalidNapConfigurationException("La capacidad de la caja NAP debe ser 8 (1x8) o 16 (1x16) puertos.");
        }
        return capacity;
    }

    private Node findOwnNode(UUID id) {
        String tenantId = TenantContext.require();
        return nodeRepository.findById(id)
                .filter(n -> n.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NodeNotFoundException(id.toString()));
    }
}
