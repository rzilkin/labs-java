package servlet;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthHelper;

import java.io.IOException;

@WebFilter(urlPatterns = "/api/v1/*")
public class BasicAuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (path.endsWith("/api/v1/auth/register") || path.endsWith("/api/v1/auth/login")) {
            chain.doFilter(request, response);
            return;
        }

        Long userId = AuthHelper.getCurrentUserId(httpRequest);
        if (userId == null) {
            logger.warn("Unauthorized request {} {}", httpRequest.getMethod(), path);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
