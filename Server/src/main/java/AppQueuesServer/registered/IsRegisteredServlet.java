package AppQueuesServer.registered;

import AppQueuesServer.BaseServlet;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

@WebServlet(name = "IsRegisteredServlet", value = "/is-registered")
public class IsRegisteredServlet extends BaseServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        JSONObject answer = new JSONObject();
        answer.put("is_registered", isRegistered(request));
        out.println(answer);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = basicDo(response);
        if (request.getParameter("sign_out") != null) {
            HttpSession session = request.getSession();
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                session.removeAttribute(names.nextElement());
            }
            JSONObject answer = new JSONObject();
            out.println(answer);
        }
    }
}
