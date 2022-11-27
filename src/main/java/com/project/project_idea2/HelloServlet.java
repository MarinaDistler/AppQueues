package com.project.project_idea2;

import java.io.*;
import java.sql.SQLException;

import org.json.JSONObject;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import com.project.project_idea2.DataBase.PostgreSQLController;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;
    private PostgreSQLController controller;

    public void init() {
        message = "Hello World!";
        controller = new PostgreSQLController();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("GET");

        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "!!!!</h1>");
        try {
            out.println("<p>" + controller.getAllNames() + "!!!!</p>");
        } catch (Exception e) {
            System.out.println(e);
        }

        out.println("</body></html>");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("POST");
        StringBuilder jb = new StringBuilder();

        try {
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
            JSONObject jsonObject = new JSONObject(jb.toString());
            controller.addUser(jsonObject.getString("login"),
                    jsonObject.getLong("password"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}