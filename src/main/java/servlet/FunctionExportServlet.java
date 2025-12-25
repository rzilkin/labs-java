package servlet;

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

@WebServlet(name = "FunctionExportServlet", urlPatterns = "/api/v1/functions/*/export")
public class FunctionExportServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionExportServlet.class);

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

        Long id = extractId(req.getPathInfo());
        if (id == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            FunctionFullDto dto = functionService.exportFunction(id, userId);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(FunctionResponse.fromFullDto(dto)));
            }
            logger.info("Экспортирована функция {} пользователем {} за {} мс",
                    id, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка экспорта функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка экспорта функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private Long extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }
        for (String segment : pathInfo.split("/")) {
            if (!segment.isBlank() && !"export".equals(segment)) {
                try {
                    return Long.parseLong(segment);
                } catch (NumberFormatException ignored) {
                    // continue looking
                }
            }
        }
        return null;
    }
}
