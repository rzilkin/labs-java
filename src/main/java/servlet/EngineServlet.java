package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.OperationService;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "EngineServlet", urlPatterns = "/api/v1/engine")
public class EngineServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(EngineServlet.class);

    private final Gson gson = new Gson();
    private final OperationService operationService = ServiceLocator.getInstance().getOperationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String engine = operationService.getEngine();
        resp.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("{\"engine\":\"" + engine + "\"}");
        }
        logger.info("Текущий движок табулирования: {}", engine);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        EngineRequest body;
        try {
            body = gson.fromJson(req.getReader(), EngineRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON движка", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.engine == null || body.engine.isBlank()) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "engine is required");
            return;
        }

        try {
            operationService.setEngine(body.engine);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write("{\"engine\":\"" + operationService.getEngine() + "\"}");
            }
            logger.info("Обновлён движок табулирования на {}", body.engine);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка обновления движка", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка обновления движка", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private static class EngineRequest {
        String engine;
    }
}