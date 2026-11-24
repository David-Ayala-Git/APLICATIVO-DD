package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.CustomRoleMapper;
import dd.conexiones.dd_ms_auth.application.service.CustomRoleService;
import dd.conexiones.dd_ms_auth.application.validation.CustomRoleValidator;
import dd.conexiones.dd_ms_auth.domain.exception.CustomRoleNotFoundException;
import dd.conexiones.dd_ms_auth.domain.exception.TenantNotFoundException;
import dd.conexiones.dd_ms_auth.domain.model.CustomRole;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.repository.CustomRoleRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.web.dto.CreateCustomRoleRequest;
import dd.conexiones.dd_ms_auth.web.dto.CustomRoleDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateCustomRoleRequest;
import dd.conexiones.mscommon.audit.Audited;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CustomRoleServiceImpl implements CustomRoleService {

    private final CustomRoleRepository customRoleRepository;
    private final TenantRepository tenantRepository;
    private final CustomRoleValidator customRoleValidator;
    private final CustomRoleMapper customRoleMapper;

    public CustomRoleServiceImpl(CustomRoleRepository customRoleRepository, TenantRepository tenantRepository,
                                  CustomRoleValidator customRoleValidator, CustomRoleMapper customRoleMapper) {
        this.customRoleRepository = customRoleRepository;
        this.tenantRepository = tenantRepository;
        this.customRoleValidator = customRoleValidator;
        this.customRoleMapper = customRoleMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomRoleDTO> findByTenant(UUID tenantId) {
        // Lectura abierta a proposito (sin validateCanManage): el propio front
        // necesita poder leer el rol de un OPERADOR/TECNICO/CARTERA para saber
        // que paginas mostrarle en el menu, y esos roles son justo a quienes no
        // se les permite crear/editar/borrar. Solo las escrituras se restringen.
        getTenantOrThrow(tenantId);
        return customRoleRepository.findByTenantId(tenantId).stream().map(customRoleMapper::toDto).toList();
    }

    @Override
    @Audited("role.create")
    public CustomRoleDTO create(UUID tenantId, CreateCustomRoleRequest request) {
        Tenant tenant = getTenantOrThrow(tenantId);
        customRoleValidator.validateCanManage(tenant);
        customRoleValidator.validateNameAvailable(tenantId, request.name());

        CustomRole role = new CustomRole();
        role.setTenantId(tenantId);
        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(request.permissions());

        return customRoleMapper.toDto(customRoleRepository.saveAndFlush(role));
    }

    @Override
    @Audited("role.update")
    public CustomRoleDTO update(UUID tenantId, UUID roleId, UpdateCustomRoleRequest request) {
        Tenant tenant = getTenantOrThrow(tenantId);
        customRoleValidator.validateCanManage(tenant);
        CustomRole role = getRoleOrThrow(tenantId, roleId);
        customRoleValidator.validateNameAvailableForUpdate(tenantId, request.name(), roleId);

        role.setName(request.name());
        role.setDescription(request.description());
        role.setPermissions(request.permissions());
        if (request.enabled() != null) {
            role.setEnabled(request.enabled());
        }

        return customRoleMapper.toDto(customRoleRepository.save(role));
    }

    @Override
    @Audited("role.status")
    public CustomRoleDTO updateStatus(UUID tenantId, UUID roleId, boolean enabled) {
        Tenant tenant = getTenantOrThrow(tenantId);
        customRoleValidator.validateCanManage(tenant);
        CustomRole role = getRoleOrThrow(tenantId, roleId);
        role.setEnabled(enabled);
        return customRoleMapper.toDto(customRoleRepository.save(role));
    }

    @Override
    @Audited("role.delete")
    public void delete(UUID tenantId, UUID roleId) {
        Tenant tenant = getTenantOrThrow(tenantId);
        customRoleValidator.validateCanManage(tenant);
        CustomRole role = getRoleOrThrow(tenantId, roleId);
        customRoleValidator.validateNotInUse(roleId);
        customRoleRepository.delete(role);
    }

    private Tenant getTenantOrThrow(UUID id) {
        return tenantRepository.findById(id).orElseThrow(() -> new TenantNotFoundException(id.toString()));
    }

    private CustomRole getRoleOrThrow(UUID tenantId, UUID roleId) {
        CustomRole role = customRoleRepository.findById(roleId)
                .orElseThrow(() -> new CustomRoleNotFoundException(roleId.toString()));
        if (!role.getTenantId().equals(tenantId)) {
            throw new CustomRoleNotFoundException(roleId.toString());
        }
        return role;
    }
}
