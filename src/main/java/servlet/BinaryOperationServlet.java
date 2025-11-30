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
import service.OperationService;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "BinaryOperationServlet", urlPatterns = "/api/v1/operations/*")
public class BinaryOperationServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(BinaryOperationServlet.class);

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

        String op = extractOperation(req.getPathInfo());
        if (op == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Operation is required");
            return;
        }

        BinaryOperationRequest body;
        try {
            body = gson.fromJson(req.getReader(), BinaryOperationRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON для бинарной операции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.leftId == null || body.rightId == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "leftId and rightId are required");
            return;
        }

        try {
            var created = operationService.applyBinaryOperation(body.leftId, body.rightId, op, userId);
            FunctionFullDto dto = functionService.findByIdAndOwner(created.getId(), userId);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(dto));
            }
            logger.info("Выполнена операция {} пользователем {} за {} мс", op, userId,
                    System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации бинарной операции {}", op, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка бинарной операции {}", op, e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private String extractOperation(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }
        for (String segment : pathInfo.split("/")) {
            if (!segment.isBlank()) {
                return segment;
            }
        }
        return null;
    }

    private static class BinaryOperationRequest {
        Long leftId;
        Long rightId;
    }
}