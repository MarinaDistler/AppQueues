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
            int status = controller.addUser(
                    jsonObject.getString("login"),
                    jsonObject.getString("password")
            );
            if (status != 200) {
                response.setStatus(status);
                response.setContentType("application/json");
                PrintWriter out = response.getWriter();
                JSONObject jsonObgect = new JSONObject();
                jsonObgect.put("error", "Логин уже существует");
                out.println(jsonObgect);
            }
        } catch (Exception e) {
            System.out.println("POST " + e);
        }
    }
}