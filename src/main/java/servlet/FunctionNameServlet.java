package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "FunctionNameServlet", urlPatterns = "/api/v1/functions/*/name")
public class FunctionNameServlet extends BaseApiServlet {
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendNotImplemented(req, resp, "PUT /functions/{id}/name");
    }
}