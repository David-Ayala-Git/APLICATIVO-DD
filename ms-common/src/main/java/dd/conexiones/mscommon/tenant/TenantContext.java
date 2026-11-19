package dd.conexiones.mscommon.tenant;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(String tenantId) {
        CURRENT.set(tenantId);
    }

    public static String get() {
        return CURRENT.get();
    }

    /** Como {@link #get()}, pero falla si no hay tenant resuelto (fail-closed). */
    public static String require() {
        String tenantId = CURRENT.get();
        if (tenantId == null || tenantId.isBlank()) {
            throw new MissingTenantException();
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
