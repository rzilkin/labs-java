package servlet;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthHelper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import dto.ErrorResponse;

public abstract class BaseApiServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Gson gson = new Gson();

    protected void sendNotImplemented(HttpServletRequest req, HttpServletResponse resp, String endpoint) throws IOException {
        logger.info("Запрос к не реализованному эндпоинту {} {}", req.getMethod(), endpoint);
        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("{\"status\":\"NOT_IMPLEMENTED\",\"endpoint\":\"" + endpoint + "\"}");
        }
    }

    protected void sendErrorResponse(HttpServletRequest req, HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status,
                HttpServletResponseErrorMapper.fromStatus(status),
                message,
                req.getRequestURI()
        );
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(body));
        }
    }

    protected Long getCurrentUserId(HttpServletRequest req) {
        return AuthHelper.getCurrentUserId(req);
    }

    protected void requireAuth(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (getCurrentUserId(req) != null) {
            return;
        }
        logger.warn("Неавторизованный запрос {} {}", req.getMethod(), req.getRequestURI());
        sendErrorResponse(req, resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}