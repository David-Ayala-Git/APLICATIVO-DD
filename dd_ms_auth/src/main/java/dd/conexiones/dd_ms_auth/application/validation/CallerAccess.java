package dd.conexiones.dd_ms_auth.application.validation;

import dd.conexiones.dd_ms_auth.domain.exception.InsufficientRoleException;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.model.enums.Role;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.mscommon.tenant.RequestUserContext;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Quien llama a los endpoints de plataforma (tenants y usuarios), segun los
 * headers que fija el gateway desde el JWT. El SUPER_ADMIN no tiene tenant y
 * administra todos los proveedores; el resto solo ve el suyo. Igual que en
 * CustomRoleValidator, TenantContext trae el slug del claim, no el UUID.
 */
@Component
public class CallerAccess {

    private final TenantRepository tenantRepository;

    public CallerAccess(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public boolean isSuperAdmin() {
        return Role.SUPER_ADMIN.name().equals(RequestUserContext.getUserRole());
    }

    public boolean isAdminIsp() {
        return Role.ADMIN_ISP.name().equals(RequestUserContext.getUserRole());
    }

    /** Proveedor de la sesion; vacio para el SUPER_ADMIN o si el slug ya no existe. */
    public Optional<Tenant> callerTenant() {
        String slug = TenantContext.get();
        return slug == null ? Optional.empty() : tenantRepository.findBySlug(slug);
    }

    public boolean ownsTenant(Tenant tenant) {
        return tenant.getSlug().equals(TenantContext.get());
    }

    public void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new InsufficientRoleException("Solo el Super Admin de la plataforma puede hacer esto");
        }
    }

    /** SUPER_ADMIN sobre cualquier proveedor, ADMIN_ISP solo sobre el suyo. */
    public void requireTenantAdmin(Tenant tenant) {
        if (isSuperAdmin() || (isAdminIsp() && ownsTenant(tenant))) {
            return;
        }
        throw new InsufficientRoleException("Tu rol no puede administrar este proveedor");
    }
}
