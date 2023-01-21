package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "InfoQueueServlet", value = "/info-queue")
public class InfoQueueServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        if (checkParameters(request, new String[]{"record_id", "queue_id"}, out)) {
            return;
        }
        Integer record_id = Integer.valueOf(request.getParameter("record_id"));
        Integer queue_id = Integer.valueOf(request.getParameter("queue_id"));
        JSONObject answer = controller.getInfoUserInQueue(record_id, queue_id);
        out.println(answer);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        JSONObject body = readRequest(request);
        if (checkBody(body, new String[]{"queue_id"}, out)) {
            return;
        }
        Integer queue_id = Integer.valueOf(body.getInt("queue_id"));
        HttpSession session = request.getSession();
        if (session.getAttribute("record_id") != null) {
            out.println(new JSONObject());
            return;
        }
        JSONObject answer = controller.addUserToQueue(queue_id, (Integer) session.getAttribute("user_id"),
                (String) session.getAttribute("user_name"));
        session.setAttribute("record_id", answer.getInt("record_id"));
        session.setAttribute("queue_id", queue_id);
        out.println(answer);
    }
}