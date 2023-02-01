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
        JSONObject answer = new JSONObject();
        if (session.getAttribute("record_id") != null) {
            Integer record_id = (Integer) session.getAttribute("record_id");
            JSONObject info = controller.checkUserStatus(record_id);
            if (info.getString("status") == "WORK") {
                answer.put("queue", session.getAttribute("queue"));
            } else {
                session.removeAttribute("record_id");
                session.removeAttribute("queue");
                session.removeAttribute("queue_id");
            }
        } else if (session.getAttribute("user_id") != null) {
            JSONObject info = controller.checkUserInQueue((int) session.getAttribute("user_id"));
            if (info.has("record_id") && info.has("queue_id") && info.has("queue_name")) {
                session.setAttribute("record_id", info.getInt("record_id"));
                session.setAttribute("queue_id", info.getInt("queue_id"));
                answer.put("queue", info.getString("queue_name"));
            }
        }
        out.println(answer);
    }
}