package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import java.io.PrintWriter;

@WebServlet(name = "FindShopsServlet", value = "/find-shops")
public class FindShopsServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = basicDo(response);
        JSONObject answer = new JSONObject();
        String name = request.getParameter("name");
        if (name.isEmpty()) {
            answer.put("error", "name should be in request");
            out.println(answer);
            return;
        }
        answer.put("shops", controller.findShops(name));
        out.println(answer);
    }
}
