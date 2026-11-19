package dd.conexiones.mscommon.tenant;

import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Segunda linea del aislamiento multitenant (guia, seccion 2.1, "regla de oro").
 * El @Filter de Hibernate solo aplica a consultas: una carga por id
 * (findById / em.find) lo ignora y devolvia registros de otro ISP. Este listener
 * corre tras CUALQUIER carga y rechaza la entidad si su tenantId no es el de la
 * peticion, asi un endpoint nuevo no puede olvidarlo.
 *
 * <p>Solo revisa entidades con {@code String getTenantId()} (el slug que viaja en
 * X-Tenant-Id). Las de dd_ms_auth guardan el UUID del tenant y quedan fuera. Sin
 * tenant en el contexto (webhooks de terceros, tareas internas) no hay nada contra
 * que comparar y no se revisa.
 */
public class TenantGuardListener implements PostLoadEventListener {

    private static final ConcurrentHashMap<Class<?>, Optional<Method>> GETTERS = new ConcurrentHashMap<>();

    @Override
    public void onPostLoad(PostLoadEvent event) {
        String current = TenantContext.get();
        if (current == null || current.isBlank()) {
            return;
        }
        Object entity = event.getEntity();
        Optional<Method> getter = GETTERS.computeIfAbsent(entity.getClass(), TenantGuardListener::findGetter);
        if (getter.isEmpty()) {
            return;
        }
        Object owner;
        try {
            owner = getter.get().invoke(entity);
        } catch (ReflectiveOperationException e) {
            // fail-closed: si no se puede leer el dueno, no se entrega el registro
            throw new IllegalStateException("No se pudo leer tenantId de " + entity.getClass().getSimpleName(), e);
        }
        if (owner != null && !current.equals(owner)) {
            throw new CrossTenantAccessException(entity.getClass().getSimpleName() + " " + event.getId());
        }
    }

    /**
     * Reflexion y no MethodHandles.publicLookup(): esa no ve clases que no son
     * publicas y devolvia "sin getter", es decir, la entidad pasaba sin revisar.
     */
    private static Optional<Method> findGetter(Class<?> type) {
        try {
            Method method = type.getMethod("getTenantId");
            if (method.getReturnType() != String.class) {
                return Optional.empty();
            }
            method.trySetAccessible();
            return Optional.of(method);
        } catch (NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}
