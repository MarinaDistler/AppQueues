package AppQueuesServer.admins;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "FindWorkerServlet", value = "/find-worker")
public class FindWorkerServlet extends BaseServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = basicDo(response);
        if (checkParameters(request, new String[]{"login"}, out)) {
            return;
        }
        String login = request.getParameter("login");
        JSONObject answer = controller.findWorker(login);
        out.println(answer);
    }
}