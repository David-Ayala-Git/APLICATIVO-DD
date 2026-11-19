package dd.conexiones.dd_ms_support.web.controller;

import dd.conexiones.dd_ms_support.application.service.TechnicianService;
import dd.conexiones.dd_ms_support.web.dto.CreateTechnicianRequest;
import dd.conexiones.dd_ms_support.web.dto.TechnicianDTO;
import dd.conexiones.dd_ms_support.web.dto.UpdateTechnicianStatusRequest;
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
@RequestMapping("/api/technicians")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("support")
public class TechnicianController {

    private final TechnicianService technicianService;

    public TechnicianController(TechnicianService technicianService) {
        this.technicianService = technicianService;
    }

    @GetMapping
    public List<TechnicianDTO> findAll() {
        return technicianService.findAll();
    }

    @GetMapping("/{id}")
    public TechnicianDTO findById(@PathVariable UUID id) {
        return technicianService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TechnicianDTO create(@Valid @RequestBody CreateTechnicianRequest request) {
        return technicianService.create(request);
    }

    @PostMapping("/{id}/status")
    public TechnicianDTO updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateTechnicianStatusRequest request) {
        return technicianService.updateStatus(id, request);
    }
}
