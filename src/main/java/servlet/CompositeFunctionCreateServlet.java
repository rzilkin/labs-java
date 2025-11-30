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

@WebServlet(name = "CompositeFunctionCreateServlet", urlPatterns = "/api/v1/functions/composite")
public class CompositeFunctionCreateServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(CompositeFunctionCreateServlet.class);

    private final Gson gson = new Gson();
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

        CreateCompositeRequest body;
        try {
            body = gson.fromJson(req.getReader(), CreateCompositeRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON для составной функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || isBlank(body.name) || body.components == null || body.components.isEmpty()) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "name and components are required");
            return;
        }

        try {
            FunctionFullDto dto = functionService.createComposite(userId, body.name, body.components);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(dto));
            }
            logger.info("Создана составная функция {} пользователем {} за {} мс",
                    dto.getSummary().getId(), userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации составной функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при создании составной функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class CreateCompositeRequest {
        String name;
        List<Long> components;
    }
}