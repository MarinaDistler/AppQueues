package AppQueuesServer.admins;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "EditQueueServlet", value = "/edit-queue")
public class StatisticServlet extends BaseServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        checkRegistered(request, out);
        if (checkParameters(request, new String[]{"queue_name"}, out)) {
            return;
        }
        String queue_name = request.getParameter("queue_name");
        HttpSession session = request.getSession();
        Integer user_id = (Integer) session.getAttribute("user_id");
        JSONObject answer = controller.infoQueue(queue_name, user_id);
        out.println(answer);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        checkRegistered(request, out);
        JSONObject body = readRequest(request);
        if (checkBody(body, new String[]{"name", "workers", "new"}, out)) {
            return;
        }
        String name = body.getString("name");
        Boolean is_new = body.getBoolean("new");
        JSONArray workers_jsonarray = body.getJSONArray("workers");
        String[] workers = new String[workers_jsonarray.length()];
        for (int i = 0; i < workers_jsonarray.length(); i++) {
            workers[i] = (String) workers_jsonarray.get(i);
        }
        HttpSession session = request.getSession();
        Integer user_id = (Integer) session.getAttribute("user_id");
        JSONObject answer;
        if (is_new) {
            answer = controller.createQueue(name, user_id, workers);
        } else {
            if (checkBody(body, new String[]{"old_name"}, out)) {
                return;
            }
            String old_name = body.getString("old_name");
            answer = controller.updateQueue(old_name, user_id, name, workers);
        }
        out.println(answer);
    }
}
