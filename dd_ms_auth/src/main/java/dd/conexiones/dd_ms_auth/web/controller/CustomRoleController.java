package dd.conexiones.dd_ms_auth.web.controller;

import dd.conexiones.dd_ms_auth.application.service.CustomRoleService;
import dd.conexiones.dd_ms_auth.web.dto.CreateCustomRoleRequest;
import dd.conexiones.dd_ms_auth.web.dto.CustomRoleDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateCustomRoleRequest;
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

/**
 * Roles personalizados de un proveedor. Solo SUPER_ADMIN o el ADMIN_ISP del
 * propio proveedor pueden crearlos, editarlos o borrarlos (CustomRoleValidator).
 */
@RestController
@RequestMapping("/api/auth/tenants/{tenantId}/roles")
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    public CustomRoleController(CustomRoleService customRoleService) {
        this.customRoleService = customRoleService;
    }

    @GetMapping
    public List<CustomRoleDTO> findAll(@PathVariable UUID tenantId) {
        return customRoleService.findByTenant(tenantId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomRoleDTO create(@PathVariable UUID tenantId, @Valid @RequestBody CreateCustomRoleRequest request) {
        return customRoleService.create(tenantId, request);
    }

    @PutMapping("/{roleId}")
    public CustomRoleDTO update(@PathVariable UUID tenantId, @PathVariable UUID roleId,
                                 @Valid @RequestBody UpdateCustomRoleRequest request) {
        return customRoleService.update(tenantId, roleId, request);
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID tenantId, @PathVariable UUID roleId) {
        customRoleService.delete(tenantId, roleId);
    }
}
