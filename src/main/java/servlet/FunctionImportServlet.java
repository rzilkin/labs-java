package servlet;

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

@WebServlet(name = "FunctionImportServlet", urlPatterns = "/api/v1/functions/import")
public class FunctionImportServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionImportServlet.class);

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

        FunctionFullDto importData;
        try {
            importData = gson.fromJson(req.getReader(), FunctionFullDto.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON при импорте функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (importData == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Import data is required");
            return;
        }

        try {
            FunctionFullDto created = functionService.importFunction(userId, importData);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(created));
            }
            logger.info("Импортирована функция {} пользователем {} за {} мс",
                    created.getId(), userId, System.currentTimeMillis() - start);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации при импорте функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Функция с таким именем уже существует", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_CONFLICT, e.getMessage());
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при импорте функции", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }
}
