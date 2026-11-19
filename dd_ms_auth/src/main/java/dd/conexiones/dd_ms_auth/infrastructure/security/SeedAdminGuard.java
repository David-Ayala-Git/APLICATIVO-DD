package dd.conexiones.dd_ms_auth.infrastructure.security;

import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import dd.conexiones.dd_ms_auth.domain.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * V2__seed_admin.sql crea el SUPER_ADMIN con una contrasena publicada en el propio
 * repositorio. No se puede editar esa migracion (Flyway ya la aplico en todas las
 * bases), asi que al arrancar se revisa si algun usuario sigue con ese hash:
 * <ul>
 *   <li>Si hay SUPER_ADMIN_PASSWORD, se rota a esa clave en TODOS los usuarios que
 *       tengan ese hash - no solo el admin de V2: cualquier cuenta creada copiando su
 *       hash comparte la misma clave publicada (una sola vez: despues el
 *       hash ya no coincide y esto no vuelve a hacer nada).</li>
 *   <li>Si no, en pdn el servicio NO arranca; en dev solo se advierte en el log.</li>
 * </ul>
 */
@Component
public class SeedAdminGuard implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedAdminGuard.class);

    /** Hash exacto de V2__seed_admin.sql; comparar el hash evita poner la clave en el codigo. */
    static final String SEED_PASSWORD_HASH = "$2a$10$BCxGCYwUpVnGIgmyjM61wOvBg2cKFvWXQIx7jKU.PH8RQQmycW2B6";
    static final int MIN_PASSWORD_LENGTH = 12;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String newPassword;
    private final boolean requireRotation;

    public SeedAdminGuard(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder,
                          @Value("${security.bootstrap.super-admin-password:}") String newPassword,
                          @Value("${security.bootstrap.require-rotation:false}") boolean requireRotation) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.newPassword = newPassword;
        this.requireRotation = requireRotation;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<AppUser> seeded = appUserRepository.findByPasswordHash(SEED_PASSWORD_HASH);
        if (seeded.isEmpty()) {
            return;
        }
        String emails = seeded.stream().map(AppUser::getEmail).toList().toString();

        if (newPassword != null && !newPassword.isBlank()) {
            if (newPassword.length() < MIN_PASSWORD_LENGTH) {
                throw new IllegalStateException("SUPER_ADMIN_PASSWORD debe tener al menos "
                        + MIN_PASSWORD_LENGTH + " caracteres");
            }
            String hash = passwordEncoder.encode(newPassword);
            seeded.forEach(user -> user.setPasswordHash(hash));
            appUserRepository.saveAll(seeded);
            log.info("Contrasena de fabrica rotada con SUPER_ADMIN_PASSWORD para {}", emails);
            return;
        }

        String message = "Los usuarios " + emails + " siguen con la contrasena de fabrica de"
                + " V2__seed_admin.sql. Define SUPER_ADMIN_PASSWORD (min. " + MIN_PASSWORD_LENGTH
                + " caracteres) para rotarla al arrancar.";
        if (requireRotation) {
            throw new IllegalStateException(message);
        }
        log.warn("{} En pdn el servicio no arrancaria asi.", message);
    }
}
