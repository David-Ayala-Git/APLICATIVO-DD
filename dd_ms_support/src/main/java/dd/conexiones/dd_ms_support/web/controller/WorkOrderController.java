package dd.conexiones.dd_ms_support.web.controller;

import dd.conexiones.dd_ms_support.application.service.WorkOrderService;
import dd.conexiones.dd_ms_support.web.dto.CreateWorkOrderRequest;
import dd.conexiones.dd_ms_support.web.dto.WorkOrderDTO;
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
@RequestMapping("/api/technicians/orders")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("support")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public List<WorkOrderDTO> findAll() {
        return workOrderService.findAll();
    }

    @GetMapping("/{id}")
    public WorkOrderDTO findById(@PathVariable UUID id) {
        return workOrderService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderDTO create(@Valid @RequestBody CreateWorkOrderRequest request) {
        return workOrderService.create(request);
    }

    @PostMapping("/{id}/complete")
    public WorkOrderDTO complete(@PathVariable UUID id) {
        return workOrderService.complete(id);
    }
}
