package servlet;

import com.google.gson.JsonSyntaxException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.FunctionService;
import service.ServiceLocator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "AnalyticFunctionUpdateServlet", urlPatterns = "/api/v1/functions/analytic/*")
public class AnalyticFunctionUpdateServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticFunctionUpdateServlet.class);

    private final FunctionService functionService = ServiceLocator.getInstance().getFunctionService();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long start = System.currentTimeMillis();
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Long userId = getCurrentUserId(req);
        if (userId == null) {
            requireAuth(req, resp);
            return;
        }

        Long id = extractId(req.getPathInfo());
        if (id == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        UpdateAnalyticRequest body;
        try {
            body = gson.fromJson(req.getReader(), UpdateAnalyticRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON при обновлении аналитической функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.expression == null || body.expression.isBlank()) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "expression is required");
            return;
        }

        try {
            functionService.updateAnalyticExpression(id, userId, body.expression);
            resp.setStatus(HttpServletResponse.SC_OK);
            logger.info("Обновлена аналитическая функция {} пользователем {} за {} мс",
                    id, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка обновления аналитической функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка обновления аналитической функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private Long extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }
        for (String segment : pathInfo.split("/")) {
            if (!segment.isBlank()) {
                try {
                    return Long.parseLong(segment);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static class UpdateAnalyticRequest {
        String expression;
    }
}
