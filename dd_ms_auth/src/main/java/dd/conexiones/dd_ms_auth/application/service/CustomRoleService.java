package dd.conexiones.dd_ms_auth.application.service;

import dd.conexiones.dd_ms_auth.web.dto.CreateCustomRoleRequest;
import dd.conexiones.dd_ms_auth.web.dto.CustomRoleDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateCustomRoleRequest;

import java.util.List;
import java.util.UUID;

public interface CustomRoleService {
    List<CustomRoleDTO> findByTenant(UUID tenantId);

    CustomRoleDTO create(UUID tenantId, CreateCustomRoleRequest request);

    CustomRoleDTO update(UUID tenantId, UUID roleId, UpdateCustomRoleRequest request);

    void delete(UUID tenantId, UUID roleId);
}
