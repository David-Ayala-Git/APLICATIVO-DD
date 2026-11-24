package dd.conexiones.dd_ms_auth.application.validation;

import dd.conexiones.dd_ms_auth.domain.exception.CustomRoleNotFoundException;
import dd.conexiones.dd_ms_auth.domain.exception.DuplicateEmailException;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidUserTenantException;
import dd.conexiones.dd_ms_auth.domain.model.CustomRole;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.CustomRoleRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserValidator {

    private final AppUserRepository appUserRepository;
    private final CustomRoleRepository customRoleRepository;

    public UserValidator(AppUserRepository appUserRepository, CustomRoleRepository customRoleRepository) {
        this.appUserRepository = appUserRepository;
        this.customRoleRepository = customRoleRepository;
    }

    public void validateEmailAvailable(String email) {
        if (appUserRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }

    /** El rol personalizado debe existir y pertenecer al mismo tenant que el usuario. */
    public void validateCustomRole(UUID tenantId, UUID customRoleId) {
        if (customRoleId == null) {
            return;
        }
        CustomRole role = customRoleRepository.findById(customRoleId)
                .orElseThrow(() -> new CustomRoleNotFoundException(customRoleId.toString()));
        if (tenantId == null || !role.getTenantId().equals(tenantId)) {
            throw new CustomRoleNotFoundException(customRoleId.toString());
        }
        if (!role.isEnabled()) {
            throw new InvalidUserTenantException("El rol personalizado esta deshabilitado");
        }
    }
}
