package servlet;

import com.google.gson.Gson;
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

@WebServlet(name = "FunctionDetailsServlet", urlPatterns = "/api/v1/functions/*")
public class FunctionDetailsServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDetailsServlet.class);

    private final Gson gson = new Gson();
    private final FunctionService functionService = ServiceLocator.getInstance().getFunctionService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWithDetails(req, resp, false);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handleWithDetails(req, resp, true);
    }

    private void handleWithDetails(HttpServletRequest req, HttpServletResponse resp, boolean delete) throws IOException {
        long start = System.currentTimeMillis();
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Long userId = getCurrentUserId(req);
        if (userId == null) {
            requireAuth(req, resp);
            return;
        }

        Long id = extractId(req);
        if (id == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            if (delete) {
                functionService.deleteById(id, userId);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                logger.info("Удалена функция {} пользователем {} за {} мс", id, userId, System.currentTimeMillis() - start);
                return;
            }

            FunctionFullDto dto = functionService.findByIdAndOwner(id, userId);
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(dto));
            }
            logger.info("Получены детали функции {} пользователем {} за {} мс", id, userId,
                    System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка запроса функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Ошибка сервера при обработке функции {}", id, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private Long extractId(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }
        String[] segments = pathInfo.split("/");
        for (String segment : segments) {
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
}