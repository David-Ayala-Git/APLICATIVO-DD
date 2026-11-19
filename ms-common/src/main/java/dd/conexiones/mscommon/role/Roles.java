package dd.conexiones.mscommon.role;

/**
 * Valores que llegan en X-User-Role. Los cinco primeros son el enum fijo de
 * auth.app_user.role (ver ddc_ms_auth); SERVICE lo envian solo las llamadas
 * internas entre microservicios (via Consul, sin pasar por el gateway), que no
 * actuan en nombre de un usuario. El gateway descarta cualquier X-User-Role
 * que mande el cliente, asi que SERVICE no se puede suplantar desde afuera.
 */
public final class Roles {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ADMIN_ISP = "ADMIN_ISP";
    public static final String OPERADOR = "OPERADOR";
    public static final String TECNICO = "TECNICO";
    public static final String CARTERA = "CARTERA";
    public static final String SERVICE = "SERVICE";

    private Roles() {
    }
}
