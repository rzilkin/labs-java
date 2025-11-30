package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dto.FunctionFullDto;
import dto.PointDto;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.FunctionService;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "TabulatedManualCreateServlet", urlPatterns = "/api/v1/functions/tabulated/manual")
public class TabulatedManualCreateServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedManualCreateServlet.class);

    private final Gson gson = new Gson();
    private final FunctionService functionService = ServiceLocator.getInstance().getFunctionService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long start = System.currentTimeMillis();
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Long userId = getCurrentUserId(req);
        if (userId == null) {
            requireAuth(req, resp);
            return;
        }

        CreateManualRequest body;
        try {
            body = gson.fromJson(req.getReader(), CreateManualRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Невалидный JSON при создании табулированной функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || isBlank(body.name) || body.points == null || body.points.size() < 2) {
            logger.warn("Ошибочные данные для табулированной функции: {}", body);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "name and at least two points are required");
            return;
        }

        try {
            FunctionFullDto created = functionService.createTabulatedManual(userId, body.name, body.points);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(created));
            }
            logger.info("Создана табулированная функция {} пользователем {} за {} мс", created.getSummary().getId(),
                    userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации табулированной функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при создании табулированной функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class CreateManualRequest {
        String name;
        List<PointDto> points;
    }
}