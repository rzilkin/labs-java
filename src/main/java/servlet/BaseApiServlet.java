package servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public abstract class BaseApiServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected void sendNotImplemented(HttpServletRequest req, HttpServletResponse resp, String endpoint) throws IOException {
        logger.info("Запрос к не реализованному эндпоинту {} {}", req.getMethod(), endpoint);
        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("{\"status\":\"NOT_IMPLEMENTED\",\"endpoint\":\"" + endpoint + "\"}");
        }
    }
}