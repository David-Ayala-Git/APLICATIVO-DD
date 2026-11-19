package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.UserMapper;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidCredentialsException;
import dd.conexiones.dd_ms_auth.domain.exception.TooManyLoginAttemptsException;
import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.infrastructure.security.JwtService;
import dd.conexiones.dd_ms_auth.infrastructure.security.LoginAttemptGuard;
import dd.conexiones.dd_ms_auth.web.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {

    private final AppUserRepository users = mock(AppUserRepository.class);
    private final JwtService jwt = mock(JwtService.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final LoginAttemptGuard guard = new LoginAttemptGuard();
    private final AuthServiceImpl service = new AuthServiceImpl(users, mock(TenantRepository.class),
            encoder, jwt, mock(UserMapper.class), guard);

    @Test
    void trasCincoClavesMalasResponde429AunqueLaSextaSeaCorrecta() {
        AppUser user = new AppUser();
        user.setEmail("op@isp.co");
        user.setPasswordHash(encoder.encode("clave-correcta"));
        when(users.findByEmail("op@isp.co")).thenReturn(Optional.of(user));

        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.login(new LoginRequest("op@isp.co", "mala"), "1.1.1.1"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
        assertThatThrownBy(() -> service.login(new LoginRequest("op@isp.co", "clave-correcta"), "1.1.1.1"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void correoInexistenteCuentaComoFalloYNoRevelaNada() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());
        for (int i = 0; i < 5; i++) {
            assertThatThrownBy(() -> service.login(new LoginRequest("nadie@isp.co", "x"), "2.2.2.2"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
        assertThatThrownBy(() -> service.login(new LoginRequest("nadie@isp.co", "x"), "2.2.2.2"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void ingresoCorrectoEmiteToken() {
        AppUser user = new AppUser();
        user.setEmail("op@isp.co");
        user.setPasswordHash(encoder.encode("clave-correcta"));
        when(users.findByEmail("op@isp.co")).thenReturn(Optional.of(user));

        service.login(new LoginRequest("op@isp.co", "clave-correcta"), "1.1.1.1");
        verify(jwt).generate(any(), any());
    }
}
