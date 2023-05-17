package AppQueuesServer.registered;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends BaseServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        JSONObject body = readRequest(request);
        if (checkBody(body, new String[]{"login", "password"}, out)) {
            return;
        }
        JSONObject answer = controller.loginUser(body.getString("login"), body.getString("password"));
        if (answer.has("user_id")) {
            request.getSession().setAttribute("user_id", answer.getInt("user_id"));
            answer.remove("user_id");
        } else {
            answer.put("notification", "Invalid login or password!");
        }
        out.println(answer);
    }
}
