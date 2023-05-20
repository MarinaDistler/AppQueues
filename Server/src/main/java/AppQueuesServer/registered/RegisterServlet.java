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

@WebServlet(name = "RegisterServlet", value = "/register")
public class RegisterServlet extends BaseServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        JSONObject body = readRequest(request);
        if (checkBody(body, new String[]{"login", "password"}, out)) {
            return;
        }
        String shop_name = null;
        if (body.has("shop_name")) {
            shop_name = body.getString("shop_name");
        }
        Integer alert_time = 5;
        if (body.has("alert_time")) {
            alert_time = body.getInt("alert_time");
        }
        JSONObject answer = controller.createUser(body.getString("login"), body.getString("password"),
                shop_name, alert_time);
        if (answer.has("user_id")) {
            request.getSession().setAttribute("user_id", answer.getInt("user_id"));
            answer.remove("user_id");
        }
        out.println(answer);
    }
}
