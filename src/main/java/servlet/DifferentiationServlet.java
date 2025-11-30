package servlet;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.FunctionService;
import service.OperationService;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "DifferentiationServlet", urlPatterns = "/api/v1/operations/differentiate/*")
public class DifferentiationServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(DifferentiationServlet.class);

    private final Gson gson = new Gson();
    private final OperationService operationService = ServiceLocator.getInstance().getOperationService();
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

        Long functionId = extractId(req.getPathInfo());
        if (functionId == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }

        try {
            var created = operationService.differentiate(functionId, userId);
            var dto = functionService.findByIdAndOwner(created.getId(), userId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(dto));
            }
            logger.info("Построена производная функции {} пользователем {} за {} мс",
                    functionId, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка производной функции {}", functionId, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка производной функции {}", functionId, e);
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
}