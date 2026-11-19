package dd.conexiones.mscommon.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Restringe el metodo/clase anotado a los roles indicados (X-User-Role que fija
 * el gateway desde el JWT). La anotacion de metodo reemplaza a la de clase, asi
 * un controller declara quien puede leer y cada accion sensible lo acota.
 * Las llamadas internas con rol SERVICE siempre pasan.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String[] value();
}
