package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.CustomRoleMapper;
import dd.conexiones.dd_ms_auth.application.validation.CustomRoleValidator;
import dd.conexiones.dd_ms_auth.domain.exception.InsufficientRoleException;
import dd.conexiones.dd_ms_auth.domain.model.CustomRole;
import dd.conexiones.dd_ms_auth.domain.model.Tenant;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.CustomRoleRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.web.dto.CustomRoleDTO;
import dd.conexiones.dd_ms_auth.web.dto.UpdateCustomRoleRequest;
import dd.conexiones.mscommon.tenant.RequestUserContext;
import dd.conexiones.mscommon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomRoleServiceImplTest {

    @Mock private CustomRoleRepository customRoleRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private AppUserRepository appUserRepository;

    private CustomRoleServiceImpl service;
    private Tenant tenant;
    private CustomRole existingRole;

    @BeforeEach
    void setUp() {
        CustomRoleValidator validator = new CustomRoleValidator(customRoleRepository, appUserRepository);
        service = new CustomRoleServiceImpl(customRoleRepository, tenantRepository, validator, new CustomRoleMapper());

        tenant = new Tenant();
        tenant.setId(UUID.randomUUID());
        tenant.setName("ISP Demo");
        tenant.setSlug("isp-demo");

        existingRole = new CustomRole();
        existingRole.setId(UUID.randomUUID());
        existingRole.setTenantId(tenant.getId());
        existingRole.setName("Cartera Norte");
        existingRole.setPermissions(List.of("billing", "payments"));
        existingRole.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        RequestUserContext.clear();
        TenantContext.clear();
    }

    @Test
    void superAdminPuedeDeshabilitarYEditarPermisosDeCualquierProveedor() {
        RequestUserContext.set(UUID.randomUUID().toString(), "SUPER_ADMIN");
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(customRoleRepository.findById(existingRole.getId())).thenReturn(Optional.of(existingRole));
        when(customRoleRepository.save(any(CustomRole.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomRoleDTO disabled = service.updateStatus(tenant.getId(), existingRole.getId(), false);
        assertThat(disabled.enabled()).isFalse();

        CustomRoleDTO updated = service.update(
                tenant.getId(),
                existingRole.getId(),
                new UpdateCustomRoleRequest("Cartera Norte", "Actualizado", List.of("billing", "payments", "reports"), true)
        );
        assertThat(updated.enabled()).isTrue();
        assertThat(updated.permissions()).containsExactly("billing", "payments", "reports");
    }

    @Test
    void adminIspPuedeDeshabilitarYEliminarEnSuPropioProveedor() {
        RequestUserContext.set(UUID.randomUUID().toString(), "ADMIN_ISP");
        TenantContext.set(tenant.getSlug());
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));
        when(customRoleRepository.findById(existingRole.getId())).thenReturn(Optional.of(existingRole));
        when(customRoleRepository.save(any(CustomRole.class))).thenAnswer(inv -> inv.getArgument(0));
        when(appUserRepository.existsByCustomRoleId(existingRole.getId())).thenReturn(false);

        CustomRoleDTO disabled = service.updateStatus(tenant.getId(), existingRole.getId(), false);
        assertThat(disabled.enabled()).isFalse();

        service.delete(tenant.getId(), existingRole.getId());
        verify(customRoleRepository).delete(existingRole);
    }

    @Test
    void adminIspNoPuedeModificarRolesDeOtroProveedor() {
        RequestUserContext.set(UUID.randomUUID().toString(), "ADMIN_ISP");
        TenantContext.set("otro-isp");
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.updateStatus(tenant.getId(), existingRole.getId(), false))
                .isInstanceOf(InsufficientRoleException.class);
    }

    @Test
    void operadorNoPuedeDeshabilitarNiEditarRoles() {
        RequestUserContext.set(UUID.randomUUID().toString(), "OPERADOR");
        TenantContext.set(tenant.getSlug());
        when(tenantRepository.findById(tenant.getId())).thenReturn(Optional.of(tenant));

        assertThatThrownBy(() -> service.updateStatus(tenant.getId(), existingRole.getId(), false))
                .isInstanceOf(InsufficientRoleException.class);
    }
}
