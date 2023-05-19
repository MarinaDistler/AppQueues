package AppQueuesServer.workers;

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
}
