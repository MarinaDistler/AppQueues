package AppQueuesServer;

import DataBase.PostgreSQLController;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BaseServlet extends HttpServlet {
    protected PostgreSQLController controller;

    public void init() {
        controller = new PostgreSQLController();
    }

    protected boolean isRegistered(HttpServletRequest request) {
        return request.getSession().getAttribute("user_id") != null;
    }

    protected boolean checkRegistered(HttpServletRequest request, PrintWriter out) {
        if (!isRegistered(request)) {
            out.println(new JSONObject().put("error", "You should be registered"));
            System.out.println("error checkRegistered: You should be registered");
            return true;
        }
        return false;
    }
    protected boolean checkParameters(HttpServletRequest request, String[] params, PrintWriter out) {
        ArrayList<String> missing_params = new ArrayList<>();
        for (String param : params) {
            if (request.getParameter(param) == null) {
                missing_params.add(param);
            }
        }
        if (!missing_params.isEmpty()) {
            out.println(new JSONObject().put("error", missing_params + " should be in params"));
            System.out.println("error checkParameters: " + missing_params + " should be in params");
            return true;
        }
        return false;
    }

    protected boolean checkSession(HttpSession session, String[] params, PrintWriter out) {
        ArrayList<String> missing_params = new ArrayList<>();
        for (String param : params) {
            if (session.getAttribute(param) == null) {
                missing_params.add(param);
            }
        }
        if (!missing_params.isEmpty()) {
            out.println(new JSONObject().put("error", missing_params + " should be in session attributes"));
            System.out.println("error checkSession: " + missing_params + " should be in session attributes");
            return true;
        }
        return false;
    }

    protected boolean checkBody(JSONObject body, String[] params, PrintWriter out) {
        ArrayList<String> missing_params = new ArrayList<>();
        for (String param : params) {
            if (!body.has(param)) {
                missing_params.add(param);
            }
        }
        if (!missing_params.isEmpty()) {
            out.println(new JSONObject().put("error", missing_params + " should be in body"));
            System.out.println("error checkBody: " + missing_params + " should be in body");
            return true;
        }
        return false;
    }

    protected PrintWriter basicDo(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        return response.getWriter();
    }

    protected JSONObject readRequest(HttpServletRequest request) throws IOException {
        StringBuilder jb = new StringBuilder();
        String line;
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
            jb.append(line);
        if (jb.toString().isEmpty()) {
            return new JSONObject();
        }
        return new JSONObject(jb.toString());
    }
}