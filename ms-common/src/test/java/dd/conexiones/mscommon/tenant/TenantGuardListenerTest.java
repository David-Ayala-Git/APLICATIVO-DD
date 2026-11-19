package dd.conexiones.mscommon.tenant;

import org.hibernate.event.spi.PostLoadEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantGuardListenerTest {

    private final TenantGuardListener listener = new TenantGuardListener();

    /** Como las entidades de dominio: tenantId es el slug que viaja en X-Tenant-Id. */
    public static class SlugEntity {
        private final String tenantId;

        public SlugEntity(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getTenantId() {
            return tenantId;
        }
    }

    /** Como AppUser en dd_ms_auth: tenantId es el UUID, no comparable con el slug. */
    public static class UuidEntity {
        public UUID getTenantId() {
            return UUID.randomUUID();
        }
    }

    public static class GlobalEntity {
    }

    @AfterEach
    void clear() {
        TenantContext.clear();
    }

    private void load(Object entity) {
        // setters y no el constructor de 4 argumentos: su orden es (id, persister, entidad, sesion)
        listener.onPostLoad(new PostLoadEvent(null).setEntity(entity).setId("id-1"));
    }

    @Test
    void permiteEntidadDelMismoTenant() {
        TenantContext.set("isp-a");
        assertThatCode(() -> load(new SlugEntity("isp-a"))).doesNotThrowAnyException();
    }

    @Test
    void rechazaEntidadDeOtroTenant() {
        TenantContext.set("isp-a");
        assertThatThrownBy(() -> load(new SlugEntity("isp-b")))
                .isInstanceOf(CrossTenantAccessException.class)
                .hasMessageContaining("SlugEntity");
    }

    @Test
    void sinTenantEnContextoNoRevisa() {
        // webhooks de pasarela / Meta: no traen tenant y resuelven el dueno desde el registro
        assertThatCode(() -> load(new SlugEntity("isp-b"))).doesNotThrowAnyException();
    }

    @Test
    void ignoraTenantIdQueNoEsSlug() {
        TenantContext.set("isp-a");
        assertThatCode(() -> load(new UuidEntity())).doesNotThrowAnyException();
    }

    @Test
    void ignoraEntidadesSinTenant() {
        TenantContext.set("isp-a");
        assertThatCode(() -> load(new GlobalEntity())).doesNotThrowAnyException();
    }
}
