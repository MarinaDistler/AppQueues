package AppQueuesServer.registered;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "CheckRegisteredServlet", value = "/check-registered")
public class CheckRegisteredServlet extends BaseServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        HttpSession session = request.getSession();
        JSONObject answer = new JSONObject();
        answer.put("is_registered", false);
        if (session.getAttribute("user_id") != null) {
            answer.put("is_registered", true);
        }
        out.println(answer);
    }
}
