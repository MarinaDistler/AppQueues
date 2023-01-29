package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "RateQueueServlet", value = "/rate-queue")
public class RateQueueServlet extends BaseServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        HttpSession session = request.getSession();
        JSONObject body = readRequest(request);
        if (checkSession(session, new String[]{"record_id"}, out) || checkBody(body, new String[]{"rating"}, out)) {
            return;
        }
        Integer record_id = (Integer) session.getAttribute("record_id");
        Float rating = body.getFloat("rating");
        JSONObject answer = controller.rateQueue(record_id, rating);
        session.removeAttribute("record_id");
        session.removeAttribute("queue_id");
        session.removeAttribute("queue");
        out.println(answer);
    }
}
