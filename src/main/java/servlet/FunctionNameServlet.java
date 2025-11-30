package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dto.FunctionSummaryDto;
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

@WebServlet(name = "FunctionNameServlet", urlPatterns = "/api/v1/functions/*/name")
public class FunctionNameServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionNameServlet.class);

    private final Gson gson = new Gson();
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

        RenameRequest body;
        try {
            body = gson.fromJson(req.getReader(), RenameRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Невалидный JSON при обновлении имени функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.name == null || body.name.isBlank()) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "name is required");
            return;
        }

        try {
            FunctionSummaryDto updated = functionService.updateName(id, userId, body.name);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(updated));
            }
            logger.info("Имя функции {} обновлено пользователем {} за {} мс", id, userId,
                    System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка обновления имени функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при обновлении имени функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private Long extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }
        String[] segments = pathInfo.split("/");
        for (String segment : segments) {
            if (!segment.isBlank() && !"name".equals(segment)) {
                try {
                    return Long.parseLong(segment);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static class RenameRequest {
        String name;
    }
}