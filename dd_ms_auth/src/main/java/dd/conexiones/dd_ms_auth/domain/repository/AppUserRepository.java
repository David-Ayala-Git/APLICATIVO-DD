package dd.conexiones.dd_ms_auth.domain.repository;

import dd.conexiones.dd_ms_auth.domain.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByCustomRoleId(UUID customRoleId);

    List<AppUser> findByTenantId(UUID tenantId);

    List<AppUser> findByPasswordHash(String passwordHash);
}
