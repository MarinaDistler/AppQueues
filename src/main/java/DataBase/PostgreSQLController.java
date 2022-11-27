package DataBase;

import java.sql.*;
import java.util.ArrayList;

public class PostgreSQLController {
    public static Connection conn;
    public static Statement stat;
    public static ResultSet rs;

    public static void Conn() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql:AppQueues",
                    "root", "247A247a");
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public static void CloseDB() {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void addUser(String login, Long password) {
        try {
            Conn();
            stat = conn.createStatement();
            String sql = "INSERT INTO users(login, password) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, login);
            statement.setLong(2, password);
            statement.execute();
            statement.close();
            stat.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        finally {
            CloseDB();
        }
    }

    public static ArrayList<String> getAllNames() throws ClassNotFoundException, SQLException
    {
        ArrayList<String> names = new ArrayList<String>();

        Conn();
        stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("select login from users");

        while (rs.next()) {
            names.add(rs.getString("login"));
        }

        rs.close();
        stat.close();
        CloseDB();

        return names;
    }

}
