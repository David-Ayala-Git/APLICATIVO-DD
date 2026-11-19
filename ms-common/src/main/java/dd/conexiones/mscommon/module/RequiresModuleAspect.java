package dd.conexiones.mscommon.module;

import dd.conexiones.mscommon.tenant.TenantContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

@Aspect
public class RequiresModuleAspect {

    private final ModuleAccessClient moduleAccessClient;

    public RequiresModuleAspect(ModuleAccessClient moduleAccessClient) {
        this.moduleAccessClient = moduleAccessClient;
    }

    @Before("@annotation(dd.conexiones.mscommon.module.RequiresModule) || @within(dd.conexiones.mscommon.module.RequiresModule)")
    public void checkModule(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RequiresModule annotation = AnnotatedElementUtils.findMergedAnnotation(signature.getMethod(), RequiresModule.class);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(joinPoint.getTarget().getClass(), RequiresModule.class);
        }
        if (annotation == null) {
            return;
        }

        String tenantSlug = TenantContext.require();
        if (!moduleAccessClient.isEnabled(tenantSlug, annotation.value())) {
            throw new ModuleDisabledException(annotation.value());
        }
    }
}
