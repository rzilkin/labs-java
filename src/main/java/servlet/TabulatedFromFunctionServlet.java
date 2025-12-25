package servlet;

import com.google.gson.Gson;
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

@WebServlet(name = "TabulatedFromFunctionServlet", urlPatterns = "/api/v1/functions/tabulated/from-function")
public class TabulatedFromFunctionServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(TabulatedFromFunctionServlet.class);

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

        CreateTabulatedFromFunctionRequest body;
        try {
            body = gson.fromJson(req.getReader(), CreateTabulatedFromFunctionRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON при создании табулированной функции из функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Request body is required");
            return;
        }

        if (body.name == null || body.name.isBlank()) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "name is required");
            return;
        }

        if (body.sourceFunctionId == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "sourceFunctionId is required");
            return;
        }

        if (body.count == null || body.count < 2) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "count must be >= 2");
            return;
        }

        if (body.from == null || body.to == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "from and to are required");
            return;
        }

        if (body.from >= body.to) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "from must be less than to");
            return;
        }

        try {
            FunctionFullDto created = functionService.createTabulatedFromFunction(
                    userId,
                    body.name,
                    body.sourceFunctionId,
                    body.count,
                    body.from,
                    body.to);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(FunctionResponse.fromFullDto(created)));
            }
            logger.info("Создана табулированная функция {} из функции {} пользователем {} за {} мс",
                    created.getId(), body.sourceFunctionId, userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации при создании табулированной функции из функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при создании табулированной функции из функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private static class CreateTabulatedFromFunctionRequest {
        String name;
        Long sourceFunctionId;
        Integer count;
        Double from;
        Double to;
    }
}
