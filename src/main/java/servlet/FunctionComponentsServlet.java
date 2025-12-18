package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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

@WebServlet(name = "FunctionComponentsServlet", urlPatterns = "/api/v1/functions/*/components")
public class FunctionComponentsServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionComponentsServlet.class);

    private final FunctionService functionService = ServiceLocator.getInstance().getFunctionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        try {
            List<Long> components = functionService.getComponents(functionId, userId);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(components));
            }
            logger.info("Получены компоненты функции {} пользователем {} за {} мс", functionId, userId,
                    System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка получения компонентов функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка компонентов функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

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

        AddComponentRequest body;
        try {
            body = gson.fromJson(req.getReader(), AddComponentRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON для добавления компонента", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.componentId == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "componentId is required");
            return;
        }

        try {
            functionService.addComponent(functionId, userId, body.componentId, body.position);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            logger.info("Добавлен компонент {} в функцию {} пользователем {} за {} мс",
                    body.componentId, functionId, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка добавления компонента в функцию {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка добавления компонента в функцию {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        RemoveComponentRequest body;
        try {
            body = gson.fromJson(req.getReader(), RemoveComponentRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON для удаления компонента", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.componentId == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "componentId is required");
            return;
        }

        try {
            functionService.removeComponent(functionId, userId, body.componentId);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            logger.info("Удалён компонент {} из функции {} пользователем {} за {} мс",
                    body.componentId, functionId, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка удаления компонента из функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка удаления компонента из функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private Long extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }
        for (String segment : pathInfo.split("/")) {
            if (!segment.isBlank() && segment.chars().allMatch(Character::isDigit)) {
                try {
                    return Long.parseLong(segment);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private static class AddComponentRequest {
        Long componentId;
        Integer position;
    }

    private static class RemoveComponentRequest {
        Long componentId;
    }
}
