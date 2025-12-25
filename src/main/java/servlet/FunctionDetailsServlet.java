package servlet;

import com.google.gson.JsonSyntaxException;
import dto.FunctionFullDto;
import dto.FunctionResponse;
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

@WebServlet(name = "FunctionDetailsServlet", urlPatterns = "/api/v1/functions/*")
public class FunctionDetailsServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDetailsServlet.class);

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

        PathInfo pathInfo = parsePath(req.getPathInfo());
        if (pathInfo.id == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            if ("components".equals(pathInfo.subPath)) {
                // GET /functions/{id}/components
                List<Long> components = functionService.getComponents(pathInfo.id, userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(gson.toJson(components));
                }
                logger.info("Получены компоненты функции {} за {} мс", pathInfo.id, System.currentTimeMillis() - start);
            } else if ("export".equals(pathInfo.subPath)) {
                // GET /functions/{id}/export
                FunctionFullDto dto = functionService.exportFunction(pathInfo.id, userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(gson.toJson(FunctionResponse.fromFullDto(dto)));
                }
                logger.info("Экспортирована функция {} за {} мс", pathInfo.id, System.currentTimeMillis() - start);
            } else if (pathInfo.subPath == null) {
                // GET /functions/{id}
                FunctionFullDto dto = functionService.findByIdAndOwner(pathInfo.id, userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(gson.toJson(FunctionResponse.fromFullDto(dto)));
                }
                logger.info("Получены детали функции {} за {} мс", pathInfo.id, System.currentTimeMillis() - start);
            } else {
                sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, "Unknown path: " + pathInfo.subPath);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка запроса функции {}", pathInfo.id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при обработке функции {}", pathInfo.id, e);
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

        PathInfo pathInfo = parsePath(req.getPathInfo());
        if (pathInfo.id == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            if ("components".equals(pathInfo.subPath)) {
                // POST /functions/{id}/components
                AddComponentRequest body;
                try {
                    body = gson.fromJson(req.getReader(), AddComponentRequest.class);
                } catch (JsonSyntaxException e) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
                    return;
                }
                if (body == null || body.componentId == null) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "componentId is required");
                    return;
                }
                functionService.addComponent(pathInfo.id, userId, body.componentId, body.position);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                logger.info("Добавлен компонент {} в функцию {} за {} мс", body.componentId, pathInfo.id,
                        System.currentTimeMillis() - start);
            } else {
                sendErrorResponse(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "POST not allowed for this path");
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка POST для функции {}", pathInfo.id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при POST функции {}", pathInfo.id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

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

        String pathInfoStr = req.getPathInfo();
        logger.debug("PUT request pathInfo: {}", pathInfoStr);
        PathInfo pathInfo = parsePath(pathInfoStr);
        if (pathInfo.id == null) {
            logger.warn("Could not parse function ID from path: {}", pathInfoStr);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            if ("name".equals(pathInfo.subPath)) {
                // PUT /functions/{id}/name
                RenameRequest body;
                try {
                    body = gson.fromJson(req.getReader(), RenameRequest.class);
                } catch (JsonSyntaxException e) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
                    return;
                }
                if (body == null || body.name == null || body.name.isBlank()) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "name is required");
                    return;
                }
                functionService.updateName(pathInfo.id, userId, body.name);
                resp.setStatus(HttpServletResponse.SC_OK);
                logger.info("Обновлено имя функции {} за {} мс", pathInfo.id, System.currentTimeMillis() - start);
            } else {
                sendErrorResponse(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "PUT not allowed for this path");
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка PUT для функции {}", pathInfo.id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Функция с таким именем уже существует", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при PUT функции {}: {}", pathInfo.id, e.getMessage(), e);
            String errorMsg = "Internal error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMsg);
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

        PathInfo pathInfo = parsePath(req.getPathInfo());
        if (pathInfo.id == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            if ("components".equals(pathInfo.subPath)) {
                // DELETE /functions/{id}/components
                RemoveComponentRequest body;
                try {
                    body = gson.fromJson(req.getReader(), RemoveComponentRequest.class);
                } catch (JsonSyntaxException e) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
                    return;
                }
                if (body == null || body.componentId == null) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "componentId is required");
                    return;
                }
                functionService.removeComponent(pathInfo.id, userId, body.componentId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Удалён компонент {} из функции {} за {} мс", body.componentId, pathInfo.id,
                        System.currentTimeMillis() - start);
            } else if (pathInfo.subPath == null) {
                // DELETE /functions/{id}
                functionService.deleteById(pathInfo.id, userId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Удалена функция {} за {} мс", pathInfo.id, System.currentTimeMillis() - start);
            } else {
                sendErrorResponse(req, resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                        "DELETE not allowed for this path");
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка DELETE для функции {}", pathInfo.id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при DELETE функции {}", pathInfo.id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private PathInfo parsePath(String path) {
        PathInfo info = new PathInfo();
        if (path == null || path.isBlank()) {
            return info;
        }

        // Remove leading slash if present
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String[] segments = cleanPath.split("/");

        for (String segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            if (info.id == null) {
                try {
                    info.id = Long.parseLong(segment);
                } catch (NumberFormatException e) {
                    // Not an ID, might be a sub-path like "analytic", "tabulated", etc.
                    return info;
                }
            } else {
                // This is the sub-path after the ID
                info.subPath = segment;
                break;
            }
        }
        return info;
    }

    private static class PathInfo {
        Long id;
        String subPath;
    }

    private static class RenameRequest {
        String name;
    }

    private static class AddComponentRequest {
        Long componentId;
        Integer position;
    }

    private static class RemoveComponentRequest {
        Long componentId;
    }
}
