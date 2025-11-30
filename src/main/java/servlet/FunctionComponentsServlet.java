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
import java.util.List;

@WebServlet(name = "FunctionComponentsServlet", urlPatterns = "/api/v1/functions/*/components")
public class FunctionComponentsServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionComponentsServlet.class);

    private final Gson gson = new Gson();
    private final FunctionService functionService = ServiceLocator.getInstance().getFunctionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, Action.GET);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, Action.UPDATE);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, Action.CLEAR);
    }

    private void handle(HttpServletRequest req, HttpServletResponse resp, Action action) throws IOException {
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
            if (action == Action.GET) {
                FunctionFullDto dto = functionService.findByIdAndOwner(functionId, userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(gson.toJson(dto.getComponents()));
                }
                logger.info("Получены компоненты функции {} пользователем {} за {} мс", functionId, userId,
                        System.currentTimeMillis() - start);
                return;
            }

            List<Long> components = null;
            if (action == Action.UPDATE) {
                UpdateComponentsRequest body;
                try {
                    body = gson.fromJson(req.getReader(), UpdateComponentsRequest.class);
                } catch (JsonSyntaxException e) {
                    logger.warn("Некорректный JSON для обновления компонентов", e);
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
                    return;
                }
                components = body == null ? null : body.components;
            } else if (action == Action.CLEAR) {
                components = List.of();
            }

            FunctionFullDto dto = functionService.updateCompositeComponents(functionId, userId, components);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(dto));
            }
            logger.info("Обновлены компоненты функции {} пользователем {} за {} мс", functionId, userId,
                    System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка обработки компонентов функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка компонентов функции {}", functionId, e);
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

    private enum Action {GET, UPDATE, CLEAR}

    private static class UpdateComponentsRequest {
        List<Long> components;
    }
}