package dd.conexiones.mscommon.role;

import dd.conexiones.mscommon.tenant.RequestUserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequiresRoleAspectTest {

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR})
    public static class Controller {
        public String read() {
            return "leido";
        }

        @RequiresRole({Roles.ADMIN_ISP})
        public String delete() {
            return "borrado";
        }
    }

    private final Controller proxy = proxied();

    private static Controller proxied() {
        AspectJProxyFactory factory = new AspectJProxyFactory(new Controller());
        factory.setProxyTargetClass(true);
        factory.addAspect(new RequiresRoleAspect());
        return factory.getProxy();
    }

    @AfterEach
    void clear() {
        RequestUserContext.clear();
    }

    private void as(String role) {
        RequestUserContext.set("user-1", role);
    }

    @Test
    void rolDeLaClasePermiteLeer() {
        as(Roles.OPERADOR);
        assertThat(proxy.read()).isEqualTo("leido");
    }

    @Test
    void rolFueraDeLaListaRecibeExcepcion() {
        as(Roles.TECNICO);
        assertThatThrownBy(proxy::read)
                .isInstanceOf(RoleNotAllowedException.class)
                .hasMessageContaining("TECNICO");
    }

    @Test
    void anotacionDelMetodoReemplazaLaDeLaClase() {
        as(Roles.OPERADOR);
        assertThatThrownBy(proxy::delete).isInstanceOf(RoleNotAllowedException.class);
        as(Roles.ADMIN_ISP);
        assertThat(proxy.delete()).isEqualTo("borrado");
    }

    @Test
    void sinRolSeRechaza() {
        assertThatThrownBy(proxy::read)
                .isInstanceOf(RoleNotAllowedException.class)
                .hasMessageContaining("X-User-Role");
    }

    @Test
    void llamadaInternaServicePasaSiempre() {
        as(Roles.SERVICE);
        assertThat(proxy.delete()).isEqualTo("borrado");
    }

    @Test
    void superAdminNoEsComodin() {
        // el SUPER_ADMIN administra la plataforma; los datos de un ISP no son suyos
        as(Roles.SUPER_ADMIN);
        assertThatThrownBy(proxy::read).isInstanceOf(RoleNotAllowedException.class);
    }
}
