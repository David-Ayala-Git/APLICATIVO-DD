package dd.conexiones.dd_ms_auth.infrastructure.security;

import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeedAdminGuardTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private AppUser seededAdmin() {
        AppUser admin = new AppUser();
        admin.setEmail("admin@ddconexiones.co");
        admin.setPasswordHash(SeedAdminGuard.SEED_PASSWORD_HASH);
        when(users.findByPasswordHash(SeedAdminGuard.SEED_PASSWORD_HASH)).thenReturn(List.of(admin));
        return admin;
    }

    @Test
    void enProduccionSinClaveNuevaNoArranca() {
        seededAdmin();
        SeedAdminGuard guard = new SeedAdminGuard(users, encoder, "", true);
        assertThatThrownBy(() -> guard.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUPER_ADMIN_PASSWORD");
    }

    @Test
    void enDesarrolloSoloAdvierte() {
        seededAdmin();
        SeedAdminGuard guard = new SeedAdminGuard(users, encoder, "", false);
        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
        verify(users, never()).saveAll(any());
    }

    @Test
    void conClaveNuevaLaRota() {
        AppUser admin = seededAdmin();
        new SeedAdminGuard(users, encoder, "una-clave-larga-y-nueva", true).run(null);

        assertThat(admin.getPasswordHash()).isNotEqualTo(SeedAdminGuard.SEED_PASSWORD_HASH);
        assertThat(encoder.matches("una-clave-larga-y-nueva", admin.getPasswordHash())).isTrue();
    }

    @Test
    void claveNuevaDemasiadoCortaSeRechaza() {
        seededAdmin();
        SeedAdminGuard guard = new SeedAdminGuard(users, encoder, "corta", true);
        assertThatThrownBy(() -> guard.run(null)).hasMessageContaining("12 caracteres");
    }

    @Test
    void sinCuentasDeFabricaNoHaceNada() {
        when(users.findByPasswordHash(SeedAdminGuard.SEED_PASSWORD_HASH)).thenReturn(List.of());
        assertThatCode(() -> new SeedAdminGuard(users, encoder, "", true).run(null)).doesNotThrowAnyException();
    }
}
