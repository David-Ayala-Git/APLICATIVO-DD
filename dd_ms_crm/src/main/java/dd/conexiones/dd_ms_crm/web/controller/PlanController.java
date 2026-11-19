package dd.conexiones.dd_ms_crm.web.controller;

import dd.conexiones.dd_ms_crm.application.service.PlanService;
import dd.conexiones.dd_ms_crm.web.dto.CreatePlanRequest;
import dd.conexiones.dd_ms_crm.web.dto.PlanDTO;
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
@RequestMapping("/api/plans")
@RequiresModule("crm")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<PlanDTO> findAll() {
        return planService.findAll();
    }

    @GetMapping("/{id}")
    public PlanDTO findById(@PathVariable UUID id) {
        return planService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDTO create(@Valid @RequestBody CreatePlanRequest request) {
        return planService.create(request);
    }
}
