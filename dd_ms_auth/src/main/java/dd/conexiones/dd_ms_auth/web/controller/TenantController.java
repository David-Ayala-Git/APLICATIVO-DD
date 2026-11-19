package dd.conexiones.dd_ms_auth.web.controller;

import dd.conexiones.dd_ms_auth.application.service.TenantService;
import dd.conexiones.dd_ms_auth.web.dto.CreateTenantRequest;
import dd.conexiones.dd_ms_auth.web.dto.ModuleStatusDTO;
import dd.conexiones.dd_ms_auth.web.dto.TenantDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateTenantBrandingRequest;
import dd.conexiones.dd_ms_auth.web.dto.UpdateTenantModuleRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/auth/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<TenantDTO> findAll() {
        return tenantService.findAll();
    }

    @GetMapping("/{id}")
    public TenantDTO findById(@PathVariable UUID id) {
        return tenantService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantDTO create(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.create(request);
    }

    @PutMapping("/{id}/modules/{moduleKey}")
    public TenantDTO updateModule(@PathVariable UUID id, @PathVariable String moduleKey,
                                   @Valid @RequestBody UpdateTenantModuleRequest request) {
        return tenantService.updateModule(id, moduleKey, request);
    }

    @PutMapping("/{id}/branding")
    public TenantDTO updateBranding(@PathVariable UUID id, @Valid @RequestBody UpdateTenantBrandingRequest request) {
        return tenantService.updateBranding(id, request);
    }

    /** Consultado por otros microservicios (via Consul) para el guard @RequiresModule. */
    @GetMapping("/by-slug/{slug}/modules/{moduleKey}")
    public ModuleStatusDTO checkModule(@PathVariable String slug, @PathVariable String moduleKey) {
        return tenantService.checkModule(slug, moduleKey);
    }
}
