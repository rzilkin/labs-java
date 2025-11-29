package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "IntegrationServlet", urlPatterns = "/api/v1/operations/integrate/*")
public class IntegrationServlet extends BaseApiServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendNotImplemented(req, resp, "POST /operations/integrate/{id}");
    }
}