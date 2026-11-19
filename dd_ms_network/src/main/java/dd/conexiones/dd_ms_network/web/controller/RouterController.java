package dd.conexiones.dd_ms_network.web.controller;

import dd.conexiones.dd_ms_network.application.service.RouterService;
import dd.conexiones.dd_ms_network.web.dto.CreateRouterRequest;
import dd.conexiones.dd_ms_network.web.dto.CutClientRequest;
import dd.conexiones.dd_ms_network.web.dto.QueueLimitRequest;
import dd.conexiones.dd_ms_network.web.dto.RouterDTO;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/network/routers")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("network")
public class RouterController {

    private final RouterService routerService;

    public RouterController(RouterService routerService) {
        this.routerService = routerService;
    }

    @GetMapping
    public List<RouterDTO> findAll() {
        return routerService.findAll();
    }

    @GetMapping("/{id}")
    public RouterDTO findById(@PathVariable UUID id) {
        return routerService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouterDTO create(@Valid @RequestBody CreateRouterRequest request) {
        return routerService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
    @PostMapping("/{id}/cut")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cut(@PathVariable UUID id, @Valid @RequestBody CutClientRequest request) {
        routerService.cutClient(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
    @PostMapping("/{id}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable UUID id, @Valid @RequestBody CutClientRequest request) {
        routerService.enableClient(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping("/{id}/queue")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void queue(@PathVariable UUID id, @Valid @RequestBody QueueLimitRequest request) {
        routerService.setQueueLimit(id, request);
    }
}
