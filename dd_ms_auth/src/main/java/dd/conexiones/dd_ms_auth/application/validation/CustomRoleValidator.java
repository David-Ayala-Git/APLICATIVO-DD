package dd.conexiones.dd_ms_auth.application.validation;

import dd.conexiones.dd_ms_auth.domain.exception.DuplicateRoleNameException;
import dd.conexiones.dd_ms_auth.domain.exception.InsufficientRoleException;
import dd.conexiones.dd_ms_auth.domain.exception.RoleInUseException;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.model.enums.Role;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.CustomRoleRepository;
import dd.conexiones.mscommon.tenant.RequestUserContext;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Quien puede crear o editar roles personalizados: solo SUPER_ADMIN (cualquier
 * proveedor) o ADMIN_ISP del proveedor dueno del rol. El resto de roles del
 * enum (OPERADOR/TECNICO/CARTERA) queda fuera a proposito - son justamente a
 * quienes se les va a asignar un rol personalizado, no quienes los definen.
 */
@Component
public class CustomRoleValidator {

    private final CustomRoleRepository customRoleRepository;
    private final AppUserRepository appUserRepository;

    public CustomRoleValidator(CustomRoleRepository customRoleRepository, AppUserRepository appUserRepository) {
        this.customRoleRepository = customRoleRepository;
        this.appUserRepository = appUserRepository;
    }

    public void validateCanManage(Tenant tenant) {
        String callerRole = RequestUserContext.getUserRole();
        boolean isSuperAdmin = Role.SUPER_ADMIN.name().equals(callerRole);
        boolean isAdminIsp = Role.ADMIN_ISP.name().equals(callerRole);

        if (!isSuperAdmin && !isAdminIsp) {
            throw new InsufficientRoleException();
        }
        // El SUPER_ADMIN administra cualquier proveedor; el ADMIN_ISP solo el
        // suyo. TenantContext guarda el slug del claim del JWT (no el UUID),
        // por eso se compara contra tenant.getSlug() y no contra tenant.getId().
        if (isAdminIsp && !tenant.getSlug().equals(TenantContext.get())) {
            throw new InsufficientRoleException();
        }
    }

    public void validateNameAvailable(UUID tenantId, String name) {
        if (customRoleRepository.existsByTenantIdAndNameIgnoreCase(tenantId, name)) {
            throw new DuplicateRoleNameException(name);
        }
    }

    public void validateNameAvailableForUpdate(UUID tenantId, String name, UUID roleId) {
        if (customRoleRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(tenantId, name, roleId)) {
            throw new DuplicateRoleNameException(name);
        }
    }

    public void validateNotInUse(UUID roleId) {
        if (appUserRepository.existsByCustomRoleId(roleId)) {
            throw new RoleInUseException(roleId.toString());
        }
    }
}
