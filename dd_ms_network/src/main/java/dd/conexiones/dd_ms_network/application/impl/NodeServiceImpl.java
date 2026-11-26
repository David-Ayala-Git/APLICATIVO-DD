package dd.conexiones.dd_ms_network.application.impl;

import dd.conexiones.dd_ms_network.application.mapper.NodeMapper;
import dd.conexiones.dd_ms_network.application.service.NodeService;
import dd.conexiones.dd_ms_network.domain.exception.NodeNotFoundException;
import dd.conexiones.dd_ms_network.domain.model.Node;
import dd.conexiones.dd_ms_network.domain.repository.NodeRepository;
import dd.conexiones.mscommon.audit.Audited;
import dd.conexiones.mscommon.tenant.TenantContext;
import dd.conexiones.mscommon.tenant.TenantFilterEnabler;
import dd.conexiones.dd_ms_network.web.dto.CreateNodeRequest;
import dd.conexiones.dd_ms_network.web.dto.NodeDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateNodeRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NodeServiceImpl implements NodeService {

    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;
    private final TenantFilterEnabler tenantFilterEnabler;

    @PersistenceContext
    private EntityManager entityManager;

    public NodeServiceImpl(NodeRepository nodeRepository, NodeMapper nodeMapper, TenantFilterEnabler tenantFilterEnabler) {
        this.nodeRepository = nodeRepository;
        this.nodeMapper = nodeMapper;
        this.tenantFilterEnabler = tenantFilterEnabler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NodeDTO> findAll() {
        tenantFilterEnabler.enable(entityManager);
        return nodeRepository.findAll().stream().map(nodeMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NodeDTO findById(UUID id) {
        tenantFilterEnabler.enable(entityManager);
        return nodeRepository.findById(id).map(nodeMapper::toDto)
                .orElseThrow(() -> new NodeNotFoundException(id.toString()));
    }

    @Override
    public NodeDTO create(CreateNodeRequest request) {
        Node node = new Node();
        node.setTenantId(TenantContext.require());
        node.setName(request.name());
        node.setType(request.type());
        node.setLatitude(request.latitude());
        node.setLongitude(request.longitude());

        return nodeMapper.toDto(nodeRepository.saveAndFlush(node));
    }

    @Override
    @Audited("node.update")
    public NodeDTO update(UUID id, UpdateNodeRequest request) {
        Node node = findOwnNode(id);
        node.setName(request.name());
        node.setType(request.type());
        node.setLatitude(request.latitude());
        node.setLongitude(request.longitude());
        if (request.status() != null) {
            node.setStatus(request.status());
        }
        return nodeMapper.toDto(nodeRepository.save(node));
    }

    @Override
    @Audited("node.delete")
    public void delete(UUID id) {
        nodeRepository.delete(findOwnNode(id));
    }

    private Node findOwnNode(UUID id) {
        String tenantId = TenantContext.require();
        return nodeRepository.findById(id)
                .filter(n -> n.getTenantId().equals(tenantId))
                .orElseThrow(() -> new NodeNotFoundException(id.toString()));
    }
}
