package dd.conexiones.dd_ms_network.domain.model;

import dd.conexiones.dd_ms_network.domain.exception.InvalidRouterOsIdException;

import java.util.regex.Pattern;

/**
 * Identificadores que viajan concatenados a un comando de la API de RouterOS
 * ("/ppp/secret/set numbers=..."). La libreria parte el comando por espacios y
 * "=", asi que un valor libre podria colar parametros extra (password=, profile=...).
 * Solo se aceptan ids internos (*1A) o nombres sin espacios, comillas ni "=".
 */
public final class RouterOsIds {

    public static final String PATTERN = "^([*][0-9A-Fa-f]{1,8}|[A-Za-z0-9][A-Za-z0-9._@-]{0,63})$";

    private static final Pattern COMPILED = Pattern.compile(PATTERN);

    private RouterOsIds() {
    }

    public static String require(String value) {
        if (value == null || !COMPILED.matcher(value).matches()) {
            throw new InvalidRouterOsIdException(value);
        }
        return value;
    }
}
