package dd.conexiones.mscommon.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TenantFilter extends OncePerRequestFilter {

    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            TenantContext.set(request.getHeader(TENANT_HEADER));
            RequestUserContext.set(request.getHeader(USER_ID_HEADER), request.getHeader(USER_ROLE_HEADER));
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            RequestUserContext.clear();
        }
    }
}
