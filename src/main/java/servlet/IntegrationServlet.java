package servlet;

import com.google.gson.Gson;
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

@WebServlet(name = "IntegrationServlet", urlPatterns = "/api/v1/operations/integrate/*")
public class IntegrationServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationServlet.class);

    private final Gson gson = new Gson();
    private final OperationService operationService = ServiceLocator.getInstance().getOperationService();

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

        Long functionId = extractId(req.getPathInfo());
        if (functionId == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        IntegrationRequest body = gson.fromJson(req.getReader(), IntegrationRequest.class);
        Integer threads = body == null ? null : body.threads;

        try {
            double result = operationService.integrate(functionId, threads, userId);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(new IntegrationResponse(result)));
            }
            logger.info("Интеграл функции {} для пользователя {} вычислен за {} мс",
                    functionId, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка интегрирования функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка интегрирования функции {}", functionId, e);
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

    private static class IntegrationRequest {
        Integer threads;
    }

    private static class IntegrationResponse {
        final double result;

        IntegrationResponse(double result) {
            this.result = result;
        }
    }
}