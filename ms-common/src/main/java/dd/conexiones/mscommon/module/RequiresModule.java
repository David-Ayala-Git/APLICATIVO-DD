package dd.conexiones.mscommon.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exige que el modulo indicado este habilitado (auth.tenant_module.enabled)
 * para el tenant actual antes de ejecutar el metodo/clase anotado. Se puede
 * usar a nivel de clase (controller) o de metodo individual - util cuando un
 * mismo controller mezcla rutas publicas (webhooks) con rutas de tenant.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModule {
    String value();
}
