package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.OperationService;
import service.ServiceLocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "EngineServlet", urlPatterns = "/api/v1/engine")
public class EngineServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(EngineServlet.class);

    private final OperationService operationService = ServiceLocator.getInstance().getOperationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String engine = mapEngineToSpec(operationService.getEngine());
        resp.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write("\"" + engine + "\"");
        }
        logger.info("Текущий движок табулирования: {}", engine);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Long userId = getCurrentUserId(req);
        if (userId == null) {
            requireAuth(req, resp);
            return;
        }

        requireRole(req, resp, "ADMIN");
        if (resp.isCommitted()) {
            return;
        }

        String body = readBody(req);
        if (body == null || body.isBlank()) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "engine is required");
            return;
        }

        String engine = body.trim().toLowerCase();
        if (!engine.equals("manual") && !engine.equals("framework")) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "engine must be 'manual' or 'framework'");
            return;
        }

        try {
            String internalEngine = mapSpecToInternal(engine);
            operationService.setEngine(internalEngine);
            String currentEngine = mapEngineToSpec(operationService.getEngine());
            resp.setStatus(HttpServletResponse.SC_OK);
            logger.info("Пользователь {} обновил движок табулирования на {}", userId, currentEngine);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка обновления движка", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка обновления движка", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String mapEngineToSpec(String internalEngine) {
        if (internalEngine == null) {
            return "manual";
        }
        if (internalEngine.toLowerCase().contains("array")) {
            return "manual";
        } else if (internalEngine.toLowerCase().contains("linked")) {
            return "framework";
        }
        return "manual";
    }

    private String mapSpecToInternal(String specEngine) {
        if ("manual".equalsIgnoreCase(specEngine)) {
            return "array";
        } else if ("framework".equalsIgnoreCase(specEngine)) {
            return "linked";
        }
        return "array";
    }
}
