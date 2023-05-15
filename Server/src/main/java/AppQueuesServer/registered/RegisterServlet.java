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

@WebServlet(name = "ProfileServlet", value = "/profile")
public class RegisterServlet extends BaseServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        HttpSession session = request.getSession();
        if (checkSession(session, new String[]{"user_id"}, out)) {
            return;
        }
        JSONObject answer = controller.getProfile((Integer) session.getAttribute("user_id"));
        out.println(answer);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        HttpSession session = request.getSession();
        JSONObject body = readRequest(request);
        if (checkSession(session, new String[]{"user_id"}, out)) {
            return;
        }
        Integer user_id = (Integer) session.getAttribute("user_id");
        JSONObject answer = new JSONObject();
        if (body.has("login")) {
            answer = controller.updateUserLogin(user_id, body.getString("login"));
        } else if (body.has("shop_name")) {
            answer = controller.updateUserShopName(user_id, body.getString("shop_name"));
        } else if (body.has("password")) {
            answer = controller.updateUserPassword(user_id, body.getString("password"));
        }
        out.println(answer);
    }
}
