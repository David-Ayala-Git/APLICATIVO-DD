package dd.conexiones.mscommon.tenant;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;

/**
 * Activa el filtro Hibernate "tenantFilter" en la sesion actual usando el tenant
 * resuelto por {@link TenantFilter}. Debe invocarse dentro de un metodo @Transactional,
 * antes de ejecutar cualquier lectura sobre una entidad multitenant.
 */
public class TenantFilterEnabler {

    public void enable(EntityManager entityManager) {
        String tenantId = TenantContext.require();
        entityManager.unwrap(Session.class)
                .enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
    }
}
