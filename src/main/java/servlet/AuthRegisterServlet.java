package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "AuthRegisterServlet", urlPatterns = "/api/v1/auth/register")
public class AuthRegisterServlet extends BaseApiServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendNotImplemented(req, resp, "POST /auth/register");
    }
}