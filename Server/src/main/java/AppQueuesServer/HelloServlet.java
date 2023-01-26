package AppQueuesServer;

import java.io.*;

import org.json.JSONObject;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import DataBase.PostgreSQLController;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;
    private PostgreSQLController controller;

    public void init() {
        controller = new PostgreSQLController();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("GET");

        response.setContentType("application/json");

        // Hello
        PrintWriter out = response.getWriter();
        try {
            JSONObject jsonObgect = new JSONObject();
            jsonObgect.put("logins", controller.getAllNames());
            out.println(jsonObgect);
        } catch (Exception e) {
            System.out.println("GET " + e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("POST");
        StringBuilder jb = new StringBuilder();

        try {
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
            JSONObject jsonObject = new JSONObject(jb.toString());
            String login = jsonObject.getString("login");
            String password = jsonObject.getString("password");
            int user_id = controller.checkUser(login, password);
            if (user_id == 0) {
                user_id = controller.addUser(login, password);
            }
            HttpSession session = request.getSession();
            session.setAttribute("user_id", user_id);
        } catch (Exception e) {
            System.out.println("POST " + e);
        }
    }
}