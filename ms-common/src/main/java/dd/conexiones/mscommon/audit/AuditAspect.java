package dd.conexiones.mscommon.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;

@Aspect
public class AuditAspect {

    private final AuditLogWriter auditLogWriter;

    public AuditAspect(AuditLogWriter auditLogWriter) {
        this.auditLogWriter = auditLogWriter;
    }

    @AfterReturning(pointcut = "@annotation(audited)", argNames = "joinPoint,audited")
    public void audit(JoinPoint joinPoint, Audited audited) {
        String detail = Arrays.toString(joinPoint.getArgs());
        auditLogWriter.record(audited.value(), detail);
    }
}
