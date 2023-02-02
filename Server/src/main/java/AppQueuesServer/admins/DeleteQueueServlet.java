package AppQueuesServer.admins;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "DeleteQueueServlet", value = "/delete-queue")
public class DeleteQueueServlet extends BaseServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        checkRegistered(request, out);
        JSONObject body = readRequest(request);
        if (checkBody(body, new String[]{"queue_name"}, out)) {
            return;
        }
        String queue_name = body.getString("queue_name");
        HttpSession session = request.getSession();
        Integer user_id = (Integer) session.getAttribute("user_id");
        JSONObject answer = controller.deleteQueue(queue_name, user_id);
        out.println(answer);
    }
}
