package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "BinaryOperationServlet", urlPatterns = "/api/v1/operations/*")
public class BinaryOperationServlet extends BaseApiServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendNotImplemented(req, resp, "POST /operations/{op}");
    }
}