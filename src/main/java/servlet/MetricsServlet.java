package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dto.PerformanceMetrics;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.MetricsService;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet(name = "MetricsServlet", urlPatterns = "/api/v1/metrics")
public class MetricsServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(MetricsServlet.class);

    private final Gson gson = new Gson();
    private final MetricsService metricsService = ServiceLocator.getInstance().getMetricsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try {
            List<PerformanceMetrics> metrics = metricsService.getAll();
            resp.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(metrics));
            }
            logger.info("Получено {} метрик производительности", metrics.size());
        } catch (Exception e) {
            logger.error("Ошибка получения метрик производительности", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        PerformanceMetrics body;
        try {
            body = gson.fromJson(req.getReader(), PerformanceMetrics.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Некорректный JSON метрики", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON");
            return;
        }

        if (body == null || body.getOperation() == null || body.getEngine() == null) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "engine and operation are required");
            return;
        }

        try {
            PerformanceMetrics saved = metricsService.saveMetric(body);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(saved));
            }
            logger.info("Сохранена метрика {}", saved.getId());
        } catch (Exception e) {
            logger.error("Ошибка сохранения метрики", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
        }
    }
}