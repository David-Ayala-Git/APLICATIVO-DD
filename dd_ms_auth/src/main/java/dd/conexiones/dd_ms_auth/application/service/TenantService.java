package dd.conexiones.dd_ms_auth.application.service;

import dd.conexiones.dd_ms_auth.web.dto.CreateTenantRequest;
import dd.conexiones.dd_ms_auth.web.dto.ModuleStatusDTO;
import dd.conexiones.dd_ms_auth.web.dto.TenantDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateTenantBrandingRequest;
import dd.conexiones.dd_ms_auth.web.dto.UpdateTenantModuleRequest;

import java.util.List;
import java.util.UUID;

public interface TenantService {
    List<TenantDTO> findAll();
    TenantDTO findById(UUID id);
    TenantDTO create(CreateTenantRequest request);
    TenantDTO updateModule(UUID tenantId, String moduleKey, UpdateTenantModuleRequest request);
    TenantDTO updateBranding(UUID tenantId, UpdateTenantBrandingRequest request);
    ModuleStatusDTO checkModule(String slug, String moduleKey);
}
