package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FindQueuesServlet", value = "/find-queues")
public class FindQueuesServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        if (checkParameters(request, new String[]{"shop_id"}, out)) {
            return;
        }
        Integer shop_id = Integer.valueOf(request.getParameter("shop_id"));
        JSONObject answer = controller.findQueues(shop_id);
        out.println(answer);
    }
}
