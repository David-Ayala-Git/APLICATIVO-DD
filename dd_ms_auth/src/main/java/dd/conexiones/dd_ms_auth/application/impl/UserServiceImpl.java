package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.UserMapper;
import dd.conexiones.dd_ms_auth.application.service.UserService;
import dd.conexiones.dd_ms_auth.application.validation.CallerAccess;
import dd.conexiones.dd_ms_auth.application.validation.UserValidator;
import dd.conexiones.dd_ms_auth.domain.exception.InsufficientRoleException;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidUserTenantException;
import dd.conexiones.dd_ms_auth.domain.exception.UserNotFoundException;
import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.model.enums.Role;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.web.dto.CreateUserRequest;
import dd.conexiones.dd_ms_auth.web.dto.UserSummaryDTO;
import dd.conexiones.mscommon.tenant.RequestUserContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Visibilidad: el SUPER_ADMIN ve y crea usuarios de cualquier proveedor; el
 * ADMIN_ISP solo los de su proveedor y nunca un SUPER_ADMIN; el resto de roles
 * solo se ve a si mismo y no crea usuarios.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;
    private final UserValidator userValidator;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CallerAccess callerAccess;

    public UserServiceImpl(AppUserRepository appUserRepository, TenantRepository tenantRepository,
                            UserValidator userValidator, UserMapper userMapper,
                            PasswordEncoder passwordEncoder, CallerAccess callerAccess) {
        this.appUserRepository = appUserRepository;
        this.tenantRepository = tenantRepository;
        this.userValidator = userValidator;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.callerAccess = callerAccess;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDTO> findAll() {
        List<AppUser> visible;
        if (callerAccess.isSuperAdmin()) {
            visible = appUserRepository.findAll();
        } else if (callerAccess.isAdminIsp()) {
            visible = callerAccess.callerTenant()
                    .map(tenant -> appUserRepository.findByTenantId(tenant.getId()))
                    .orElse(List.of());
        } else {
            UUID self = callerUserId();
            visible = self == null ? List.of() : appUserRepository.findById(self).map(List::of).orElse(List.of());
        }
        return visible.stream().map(userMapper::toDto).toList();
    }

    @Override
    public UserSummaryDTO create(CreateUserRequest request) {
        UUID tenantId = resolveTenantForNewUser(request);
        userValidator.validateEmailAvailable(request.email());
        userValidator.validateCustomRole(tenantId, request.customRoleId());

        AppUser user = new AppUser();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role());
        user.setTenantId(tenantId);
        user.setCustomRoleId(request.customRoleId());

        return userMapper.toDto(appUserRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDTO findById(UUID id) {
        return appUserRepository.findById(id)
                .filter(this::isVisible)
                .map(userMapper::toDto)
                // 404 tambien cuando existe pero es ajeno: no confirmar su existencia
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    private boolean isVisible(AppUser user) {
        if (callerAccess.isSuperAdmin()) {
            return true;
        }
        if (callerAccess.isAdminIsp()) {
            return callerAccess.callerTenant()
                    .map(tenant -> tenant.getId().equals(user.getTenantId()))
                    .orElse(false);
        }
        return user.getId().equals(callerUserId());
    }

    /** Devuelve el tenant con el que se guarda el usuario, o falla si quien llama no puede crearlo. */
    private UUID resolveTenantForNewUser(CreateUserRequest request) {
        if (callerAccess.isSuperAdmin()) {
            if (request.role() == Role.SUPER_ADMIN) {
                if (request.tenantId() != null) {
                    throw new InvalidUserTenantException("Un Super Admin es de la plataforma: no lleva tenantId");
                }
                return null;
            }
            if (request.tenantId() == null || !tenantRepository.existsById(request.tenantId())) {
                throw new InvalidUserTenantException("El rol " + request.role() + " necesita un tenantId de un proveedor existente");
            }
            return request.tenantId();
        }
        if (callerAccess.isAdminIsp()) {
            if (request.role() == Role.SUPER_ADMIN) {
                throw new InsufficientRoleException("Un Admin del ISP no puede crear usuarios Super Admin");
            }
            Tenant own = callerAccess.callerTenant()
                    .orElseThrow(() -> new InsufficientRoleException("Tu sesion no tiene un proveedor valido"));
            if (request.tenantId() != null && !Objects.equals(request.tenantId(), own.getId())) {
                throw new InsufficientRoleException("Solo puedes crear usuarios de tu propio proveedor");
            }
            return own.getId();
        }
        throw new InsufficientRoleException("Tu rol no puede crear usuarios");
    }

    private UUID callerUserId() {
        String id = RequestUserContext.getUserId();
        try {
            return id == null ? null : UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
