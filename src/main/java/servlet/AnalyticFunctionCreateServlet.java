package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dto.FunctionFullDto;
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

@WebServlet(name = "AnalyticFunctionCreateServlet", urlPatterns = "/api/v1/functions/analytic")
public class AnalyticFunctionCreateServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticFunctionCreateServlet.class);

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

        CreateAnalyticRequest body;
        try {
            body = gson.fromJson(req.getReader(), CreateAnalyticRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON при создании аналитической функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || isBlank(body.name) || isBlank(body.expression)) {
            logger.warn("Пустые поля при создании аналитической функции: {}", body);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "name and expression are required");
            return;
        }

        try {
            FunctionFullDto created = functionService.createAnalytic(userId, body.name, body.expression);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(created));
            }
            logger.info("Создана аналитическая функция {} пользователем {} за {} мс",
                    created.getSummary().getId(), userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации при создании аналитической функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при создании аналитической функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class CreateAnalyticRequest {
        String name;
        String expression;
    }
}