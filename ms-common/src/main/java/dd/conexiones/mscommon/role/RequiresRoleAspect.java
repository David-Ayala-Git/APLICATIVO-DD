package dd.conexiones.mscommon.role;

import dd.conexiones.mscommon.tenant.RequestUserContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;

import java.util.Arrays;

/**
 * Corre antes que @RequiresModule: rechazar por rol no cuesta una llamada a
 * ddc_ms_auth, y no revela si el modulo esta activo a quien no puede usarlo.
 * RequiresModuleAspect no declara orden (= LOWEST_PRECEDENCE), asi que basta con
 * uno menos. Nunca HIGHEST_PRECEDENCE: correria antes que ExposeInvocationInterceptor
 * de Spring AOP y toda llamada anotada fallaria con "No MethodInvocation found".
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class RequiresRoleAspect {

    @Before("@annotation(dd.conexiones.mscommon.role.RequiresRole) || @within(dd.conexiones.mscommon.role.RequiresRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresRole annotation = AnnotatedElementUtils.findMergedAnnotation(signature.getMethod(), RequiresRole.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(joinPoint.getTarget().getClass(), RequiresRole.class);
        }
        if (annotation == null) {
            return;
        }

        String role = RequestUserContext.getUserRole();
        if (Roles.SERVICE.equals(role)) {
            return;
        }
        if (role == null || Arrays.stream(annotation.value()).noneMatch(role::equals)) {
            throw new RoleNotAllowedException(role);
        }
    }
}
