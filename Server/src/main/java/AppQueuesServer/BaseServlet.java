package AppQueuesServer;

import DataBase.PostgreSQLController;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class BaseServlet extends HttpServlet {
    protected PostgreSQLController controller;

    public void init() {
        controller = new PostgreSQLController();
    }

    protected PrintWriter basicDo(HttpServletResponse response) {
        response.setContentType("application/json");
        try {
            return response.getWriter();
        }  catch (Exception e) {
            System.out.println("BasicDo: " + e);
            return null;
        }
    }

    protected JSONObject readRequest(HttpServletRequest request) {
        StringBuilder jb = new StringBuilder();
        String line;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
            if (jb.toString().isEmpty()) {
                return new JSONObject();
            }
            return new JSONObject(jb.toString());
        }  catch (Exception e) {
            System.out.println("readRequest: " + e);
            return new JSONObject();
        }
    }
}