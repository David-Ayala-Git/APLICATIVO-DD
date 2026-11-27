package dd.conexiones.dd_ms_network.web.controller;

import dd.conexiones.dd_ms_network.application.service.FiberLinkService;
import dd.conexiones.dd_ms_network.web.dto.CreateFiberLinkRequest;
import dd.conexiones.dd_ms_network.web.dto.FiberLinkDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateFiberLinkRequest;
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
@RequestMapping("/api/coverage/links")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("network")
public class FiberLinkController {

    private final FiberLinkService fiberLinkService;

    public FiberLinkController(FiberLinkService fiberLinkService) {
        this.fiberLinkService = fiberLinkService;
    }

    @GetMapping
    public List<FiberLinkDTO> findAll() {
        return fiberLinkService.findAll();
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FiberLinkDTO create(@Valid @RequestBody CreateFiberLinkRequest request) {
        return fiberLinkService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
    @PutMapping("/{id}")
    public FiberLinkDTO update(@PathVariable UUID id, @Valid @RequestBody UpdateFiberLinkRequest request) {
        return fiberLinkService.update(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        fiberLinkService.delete(id);
    }
}
