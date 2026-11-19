package dd.conexiones.mscommon.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca una accion sensible (corte, reactivacion, cambio de plan/estado,
 * activacion de modulo, etc.) para que quede registrada en <schema>.audit_log
 * al completarse con exito. value() es el codigo de la accion, ej. "router.cut".
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    String value();
}
