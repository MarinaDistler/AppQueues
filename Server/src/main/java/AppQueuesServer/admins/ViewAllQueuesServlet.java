package AppQueuesServer.admins;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "ViewAllQueuesServlet", value = "/view-queues")
public class ViewAllQueuesServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        checkRegistered(request, out);
        HttpSession session = request.getSession();
        Integer user_id = (Integer) session.getAttribute("user_id");
        JSONObject answer = controller.viewAllQueues(user_id);
        out.println(answer);
    }
}
