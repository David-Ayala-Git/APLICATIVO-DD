package dd.conexiones.dd_ms_network.web.controller;

import dd.conexiones.dd_ms_network.application.service.EquipmentService;
import dd.conexiones.dd_ms_network.web.dto.AssignEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.CreateEquipmentRequest;
import dd.conexiones.dd_ms_network.web.dto.EquipmentDTO;
import dd.conexiones.dd_ms_network.web.dto.UpdateEquipmentRequest;
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
@RequestMapping("/api/inventory/equipment")
@RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.TECNICO})
@RequiresModule("network")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public List<EquipmentDTO> findAll() {
        return equipmentService.findAll();
    }

    @GetMapping("/{id}")
    public EquipmentDTO findById(@PathVariable UUID id) {
        return equipmentService.findById(id);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentDTO create(@Valid @RequestBody CreateEquipmentRequest request) {
        return equipmentService.create(request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @PutMapping("/{id}")
    public EquipmentDTO update(@PathVariable UUID id, @Valid @RequestBody UpdateEquipmentRequest request) {
        return equipmentService.update(id, request);
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        equipmentService.delete(id);
    }

    @PostMapping("/{id}/assign")
    public EquipmentDTO assign(@PathVariable UUID id, @Valid @RequestBody AssignEquipmentRequest request) {
        return equipmentService.assign(id, request);
    }
}
