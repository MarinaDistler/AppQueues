package AppQueuesServer.clients;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FindShopsServlet", value = "/find-shops")
public class FindShopsServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        if (checkParameters(request, new String[]{"name"}, out)) {
            return;
        }
        String name = request.getParameter("name");
        JSONObject answer = controller.findShops(name);
        out.println(answer);
    }
}
