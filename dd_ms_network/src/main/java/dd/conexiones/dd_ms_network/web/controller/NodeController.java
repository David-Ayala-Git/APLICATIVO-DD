package dd.conexiones.dd_ms_network.web.controller;

import dd.conexiones.dd_ms_network.application.service.NodeService;
import dd.conexiones.dd_ms_network.web.dto.AssignNapClientRequest;
import dd.conexiones.dd_ms_network.web.dto.CreateNodeRequest;
import dd.conexiones.dd_ms_network.web.dto.NodeDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateNodeRequest;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coverage/nodes")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("network")
public class NodeController {

    private final NodeService nodeService;

    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping
    public List<NodeDTO> findAll() {
        return nodeService.findAll();
    }

    @GetMapping("/{id}")
    public NodeDTO findById(@PathVariable UUID id) {
        return nodeService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NodeDTO create(@Valid @RequestBody CreateNodeRequest request) {
        return nodeService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PutMapping("/{id}")
    public NodeDTO update(@PathVariable UUID id, @Valid @RequestBody UpdateNodeRequest request) {
        return nodeService.update(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        nodeService.delete(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @PostMapping("/{id}/assign")
    public NodeDTO assignClient(@PathVariable UUID id, @Valid @RequestBody AssignNapClientRequest request) {
        return nodeService.assignClient(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @DeleteMapping("/{id}/assignments/{assignmentId}")
    public NodeDTO unassignClient(@PathVariable UUID id, @PathVariable UUID assignmentId) {
        return nodeService.unassignClient(id, assignmentId);
    }
}
