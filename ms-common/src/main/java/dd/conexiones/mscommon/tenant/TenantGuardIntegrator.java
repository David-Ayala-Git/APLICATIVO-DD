package dd.conexiones.mscommon.tenant;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;

/**
 * Registra TenantGuardListener en toda SessionFactory. Hibernate lo descubre por
 * ServiceLoader (META-INF/services), asi que basta con depender de ms-common.
 */
public class TenantGuardIntegrator implements Integrator {

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext,
                          SessionFactoryImplementor sessionFactory) {
        sessionFactory.getEventListenerRegistry()
                .appendListeners(EventType.POST_LOAD, new TenantGuardListener());
    }
}
