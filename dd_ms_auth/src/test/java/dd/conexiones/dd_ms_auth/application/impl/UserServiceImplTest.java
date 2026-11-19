package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.UserMapper;
import dd.conexiones.dd_ms_auth.application.validation.CallerAccess;
import dd.conexiones.dd_ms_auth.application.validation.UserValidator;
import dd.conexiones.dd_ms_auth.domain.exception.InsufficientRoleException;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidUserTenantException;
import dd.conexiones.dd_ms_auth.domain.exception.UserNotFoundException;
import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.model.enums.Role;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.CustomRoleRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.web.dto.CreateUserRequest;
import dd.conexiones.dd_ms_auth.web.dto.UserSummaryDTO;
import dd.conexiones.mscommon.tenant.RequestUserContext;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Quien crea y ve usuarios. Antes de esta regla cualquier rol autenticado podia
 * crear una cuenta SUPER_ADMIN: estos casos evitan que vuelva a pasar.
 */
class UserServiceImplTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final TenantRepository tenants = mock(TenantRepository.class);

    @SuppressWarnings("deprecation")
    private final UserServiceImpl service = new UserServiceImpl(users, tenants,
            new UserValidator(users, mock(CustomRoleRepository.class)), new UserMapper(),
            NoOpPasswordEncoder.getInstance(), new CallerAccess(tenants));

    private final Tenant ispA = tenant("isp-a");
    private final Tenant ispB = tenant("isp-b");

    private static Tenant tenant(String slug) {
        Tenant t = new Tenant();
        t.setId(UUID.randomUUID());
        t.setSlug(slug);
        return t;
    }

    @BeforeEach
    void setUp() {
        when(tenants.findBySlug("isp-a")).thenReturn(Optional.of(ispA));
        when(tenants.existsById(ispA.getId())).thenReturn(true);
        when(tenants.existsById(ispB.getId())).thenReturn(true);
        when(users.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
        RequestUserContext.clear();
    }

    private void as(Role role, String tenantSlug) {
        RequestUserContext.set(UUID.randomUUID().toString(), role.name());
        TenantContext.set(tenantSlug);
    }

    private CreateUserRequest request(Role role, UUID tenantId) {
        return new CreateUserRequest("nuevo@isp.co", "x".repeat(10), "Nuevo", role, tenantId, null);
    }

    @Test
    void operadorNoPuedeCrearUnSuperAdmin() {
        as(Role.OPERADOR, "isp-a");
        assertThatThrownBy(() -> service.create(request(Role.SUPER_ADMIN, null)))
                .isInstanceOf(InsufficientRoleException.class);
        verify(users, never()).save(any());
    }

    @Test
    void adminIspNoPuedeCrearUnSuperAdmin() {
        as(Role.ADMIN_ISP, "isp-a");
        assertThatThrownBy(() -> service.create(request(Role.SUPER_ADMIN, null)))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void adminIspNoPuedeCrearEnOtroIsp() {
        as(Role.ADMIN_ISP, "isp-a");
        assertThatThrownBy(() -> service.create(request(Role.OPERADOR, ispB.getId())))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void adminIspCreaSiempreEnSuPropioIsp() {
        as(Role.ADMIN_ISP, "isp-a");
        UserSummaryDTO created = service.create(request(Role.OPERADOR, null));
        assertThat(created.tenantId()).isEqualTo(ispA.getId());
    }

    @Test
    void superAdminDePlataformaNoLlevaTenant() {
        as(Role.SUPER_ADMIN, null);
        assertThatThrownBy(() -> service.create(request(Role.SUPER_ADMIN, ispA.getId())))
                .isInstanceOf(InvalidUserTenantException.class);
    }

    @Test
    void rolDeIspNecesitaUnProveedorExistente() {
        as(Role.SUPER_ADMIN, null);
        assertThatThrownBy(() -> service.create(request(Role.OPERADOR, null)))
                .isInstanceOf(InvalidUserTenantException.class);
        assertThatThrownBy(() -> service.create(request(Role.OPERADOR, UUID.randomUUID())))
                .isInstanceOf(InvalidUserTenantException.class);
    }

    @Test
    void superAdminCreaEnCualquierIsp() {
        as(Role.SUPER_ADMIN, null);
        assertThat(service.create(request(Role.ADMIN_ISP, ispB.getId())).tenantId()).isEqualTo(ispB.getId());
    }

    @Test
    void adminIspNoVeUsuariosDeOtroIsp() {
        as(Role.ADMIN_ISP, "isp-a");
        AppUser ajeno = new AppUser();
        ajeno.setId(UUID.randomUUID());
        ajeno.setTenantId(ispB.getId());
        when(users.findById(ajeno.getId())).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> service.findById(ajeno.getId())).isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void operadorSoloSeVeASiMismo() {
        AppUser yo = new AppUser();
        yo.setId(UUID.randomUUID());
        yo.setTenantId(ispA.getId());
        RequestUserContext.set(yo.getId().toString(), Role.OPERADOR.name());
        TenantContext.set("isp-a");
        when(users.findById(yo.getId())).thenReturn(Optional.of(yo));

        List<UserSummaryDTO> visible = service.findAll();

        assertThat(visible).extracting(UserSummaryDTO::id).containsExactly(yo.getId());
        verify(users, never()).findAll();
    }
}
