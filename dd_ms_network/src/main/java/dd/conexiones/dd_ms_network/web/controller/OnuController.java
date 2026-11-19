package dd.conexiones.dd_ms_network.web.controller;

import dd.conexiones.dd_ms_network.application.service.OnuService;
import dd.conexiones.dd_ms_network.web.dto.AuthorizeOnuRequest;
import dd.conexiones.dd_ms_network.web.dto.OnuDTO;
import dd.conexiones.dd_ms_network.web.dto.RegisterOnuRequest;
import dd.conexiones.dd_ms_network.web.dto.SignalUpdateRequest;
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
@RequestMapping("/api/gpon/onu")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("network")
public class OnuController {

    private final OnuService onuService;

    public OnuController(OnuService onuService) {
        this.onuService = onuService;
    }

    @GetMapping
    public List<OnuDTO> findAll() {
        return onuService.findAll();
    }

    @GetMapping("/{id}")
    public OnuDTO findById(@PathVariable UUID id) {
        return onuService.findById(id);
    }

    @GetMapping("/{id}/signal")
    public OnuDTO signal(@PathVariable UUID id) {
        return onuService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OnuDTO register(@Valid @RequestBody RegisterOnuRequest request) {
        return onuService.register(request);
    }

    @PostMapping("/authorize/{id}")
    public OnuDTO authorize(@PathVariable UUID id, @Valid @RequestBody AuthorizeOnuRequest request) {
        return onuService.authorize(id, request);
    }

    @PostMapping("/{id}/signal")
    public OnuDTO updateSignal(@PathVariable UUID id, @Valid @RequestBody SignalUpdateRequest request) {
        return onuService.updateSignal(id, request);
    }
}
