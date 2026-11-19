package dd.conexiones.mscommon.tenant;

/**
 * Identidad del usuario autenticado, propagada por el gateway via
 * X-User-Id / X-User-Role tras validar el JWT. Usada por la auditoria de
 * acciones sensibles para saber quien hizo que.
 */
public final class RequestUserContext {

    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE = new ThreadLocal<>();

    private RequestUserContext() {
    }

    public static void set(String userId, String userRole) {
        USER_ID.set(userId);
        USER_ROLE.set(userRole);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static String getUserRole() {
        return USER_ROLE.get();
    }

    public static void clear() {
        USER_ID.remove();
        USER_ROLE.remove();
    }
}
