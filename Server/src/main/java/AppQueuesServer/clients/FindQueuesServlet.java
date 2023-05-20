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
        if (checkParameters(request, new String[]{"shop"}, out)) {
            return;
        }
        String shop_name = request.getParameter("shop");
        request.getSession().setAttribute("shop_name", shop_name);
        JSONObject answer = controller.findQueues(shop_name);
        out.println(answer);
    }
}
