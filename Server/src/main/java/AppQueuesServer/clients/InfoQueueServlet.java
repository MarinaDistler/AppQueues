package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@WebServlet(name = "InfoQueueServlet", value = "/info-queue")
public class InfoQueueServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        HttpSession session = request.getSession();
        if (checkSession(session, new String[]{"record_id", "queue_id"}, out)) {
            return;
        }
        Integer record_id = (Integer) session.getAttribute("record_id");
        Integer queue_id = (Integer) session.getAttribute("queue_id");
        Boolean check_status = Boolean.valueOf(request.getParameter("check_status"));
        Boolean get_limit = Boolean.valueOf(request.getParameter("get_limit"));
        JSONObject answer = new JSONObject();
        if (check_status) {
            answer = controller.checkUserStatus(record_id);
        } else if (get_limit) {
            if (checkSession(session, new String[]{"user_id"}, out)) {
                out.println(answer);
                return;
            }
            Integer user_id = (Integer) session.getAttribute("user_id");
            answer = controller.getUserAlertTime(user_id);
        } else {
            answer = controller.getInfoUserInQueue(record_id, queue_id);
        }
        out.println(answer);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        JSONObject body = readRequest(request);
        if (request.getParameter("add_user") != null) {
            if (checkBody(body, new String[]{"queue_id", "queue"}, out)) {
                return;
            }
            Integer queue_id = body.getInt("queue_id");
            String queue = body.getString("queue");
            HttpSession session = request.getSession();
            if (session.getAttribute("record_id") != null) {
                out.println(new JSONObject());
                return;
            }
            JSONObject answer = controller.addUserToQueue(queue_id, (Integer) session.getAttribute("user_id"),
                    (String) session.getAttribute("user_name"));
            session.setAttribute("record_id", answer.getInt("record_id"));
            session.setAttribute("queue_id", queue_id);
            session.setAttribute("queue_name", queue);
            answer.remove("record_id");
            answer.remove("queue");
            out.println(answer);
        } else {
            HttpSession session = request.getSession();
            if (checkSession(session, new String[]{"record_id", "queue_id"}, out)) {
                return;
            }
            Integer record_id = (Integer) session.getAttribute("record_id");
            Integer queue_id = (Integer) session.getAttribute("queue_id");
            JSONObject answer = new JSONObject();
            if (request.getParameter("exit") != null) {
                answer = controller.exitUserFromQueue(record_id);
            } else if (request.getParameter("skip") != null) {
                answer = controller.skipPlaceUserInQueue(record_id, queue_id);
            }
            out.println(answer);
        }
    }
}
