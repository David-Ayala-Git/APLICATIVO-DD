package dd.conexiones.dd_ms_auth.application.validation;

import dd.conexiones.dd_ms_auth.domain.exception.DuplicateSlugException;
import dd.conexiones.dd_ms_auth.domain.repository.TenantRepository;
import org.springframework.stereotype.Component;

@Component
public class TenantValidator {

    private final TenantRepository tenantRepository;

    public TenantValidator(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void validateSlugAvailable(String slug) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException(slug);
        }
    }
}
