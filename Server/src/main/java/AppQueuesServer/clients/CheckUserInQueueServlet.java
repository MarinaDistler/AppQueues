package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "CheckUserInQueueServlet", value = "/check-user-in-queue")
public class CheckUserInQueueServlet extends BaseServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        HttpSession session = request.getSession();
        JSONObject answer = new JSONObject().put("is_in_queue", false);
        if (session.getAttribute("record_id") != null) {
            answer.put("is_in_queue", true);
        } else if (session.getAttribute("user_id") != null) {
            JSONObject info = controller.checkUserInQueue((int) session.getAttribute("user_id"));
            if (info.has("record_id") && info.has("queue_id")) {
                session.setAttribute("record_id", info.getInt("record_id"));
                session.setAttribute("queue_id", info.getInt("queue_id"));
                answer.put("is_in_queue", true);
            }
        }
        out.println(answer);
    }
}
