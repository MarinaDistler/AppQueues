package AppQueuesServer.admins;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "EditQueueServlet", value = "/edit-queue")
public class EditQueueServlet extends BaseServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
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
        if (checkSession(session, new String[]{"user_id"}, out)) {
            return;
        }
        Integer user_id = (Integer) session.getAttribute("user_id");
        JSONObject answer = new JSONObject();
        if (is_new) {
            answer = controller.createQueue(name, user_id, workers);
        } else {
            if (checkBody(body, new String[]{"queue_id"}, out)) {
                return;
            }
            Integer queue_id = body.getInt("queue_id");
            answer = controller.updateQueue(queue_id, name, workers);
        }
        out.println(answer);
    }
}
