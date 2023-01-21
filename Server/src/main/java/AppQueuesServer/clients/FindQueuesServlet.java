package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.PrintWriter;

@WebServlet(name = "FindQueuesServlet", value = "/find-queues")
public class FindQueuesServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = basicDo(response);
        JSONObject answer = new JSONObject();
        if (request.getParameter("shop_id").isEmpty()) {
            answer.put("error", "shop should be in request");
            out.println(answer);
            return;
        }
        Integer shop_id = Integer.valueOf(request.getParameter("shop_id"));
        answer.put("queues", controller.findQueues(shop_id));
        out.println(answer);
    }
}
