package dd.conexiones.dd_ms_auth.application.impl;

import dd.conexiones.dd_ms_auth.application.mapper.UserMapper;
import dd.conexiones.dd_ms_auth.application.service.AuthService;
import dd.conexiones.dd_ms_auth.domain.exception.InvalidCredentialsException;
import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import dd.conexiones.dd_ms_auth.infrastructure.security.JwtService;
import dd.conexiones.dd_ms_auth.infrastructure.security.LoginAttemptGuard;
import dd.conexiones.dd_ms_auth.web.dto.LoginRequest;
import dd.conexiones.dd_ms_auth.web.dto.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final LoginAttemptGuard loginAttemptGuard;
    /** Hash contra el que se compara cuando el correo no existe (ver login). */
    private final String dummyHash;

    public AuthServiceImpl(AppUserRepository appUserRepository, TenantRepository tenantRepository,
                            PasswordEncoder passwordEncoder, JwtService jwtService, UserMapper userMapper,
                            LoginAttemptGuard loginAttemptGuard) {
        this.appUserRepository = appUserRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.loginAttemptGuard = loginAttemptGuard;
        this.dummyHash = passwordEncoder.encode("usuario-inexistente");
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        loginAttemptGuard.checkAllowed(request.email(), clientIp);

        AppUser user = appUserRepository.findByEmail(request.email()).orElse(null);
        // Con correo inexistente tambien se paga un bcrypt: si no, el tiempo de
        // respuesta revela que correos estan registrados.
        boolean valid = passwordEncoder.matches(request.password(),
                user == null ? dummyHash : user.getPasswordHash());
        if (user == null || !valid) {
            loginAttemptGuard.recordFailure(request.email(), clientIp);
            throw new InvalidCredentialsException();
        }
        loginAttemptGuard.recordSuccess(request.email(), clientIp);

        // El JWT propaga el slug del tenant (mismo valor usado como X-Tenant-Id en
        // toda la plataforma), nunca el UUID interno de auth.tenant.id.
        String tenantSlug = user.getTenantId() == null ? null
                : tenantRepository.findById(user.getTenantId())
                        .map(dd.conexiones.dd_ms_auth.domain.model.Tenant::getSlug)
                        .orElse(null);

        String token = jwtService.generate(user, tenantSlug);
        return LoginResponse.bearer(token, jwtService.expirationSeconds(), userMapper.toDto(user));
    }
}
