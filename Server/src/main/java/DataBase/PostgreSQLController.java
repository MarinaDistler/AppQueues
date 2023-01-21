package DataBase;

import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Integer> findShops(String name) {
        Conn();
        Map<String, Integer> shops = new HashMap<>();
        try {
            stat = conn.createStatement();
            String sql = "select shop_name as name, shop_id as id from shops, queues where shop_name like ? and " +
                    "shop_id=owner_shop_id order by shop_name";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + name + "%");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                shops.put(rs.getString("name"), rs.getInt("id"));
            }
            rs.close();
            statement.close();
            stat.close();
        } catch (SQLException e) {
            System.out.println("Database findShops: " + e);
        }
        CloseDB();
        return shops;
    }

    public Map<String, Integer> findQueues(Integer shop_id) {
        Conn();
        Map<String, Integer> queues = new HashMap<>();
        try {
            stat = conn.createStatement();
            String sql = "select queue_name as name, queue_id as id from queues where owner_shop_id=? order by number";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, shop_id);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                queues.put(rs.getString("name"), rs.getInt("id"));
            }
            rs.close();
            statement.close();
            stat.close();
        } catch (SQLException e) {
            System.out.println("Database findQueues: " + e);
        }
        CloseDB();
        return queues;
    }

    public static int addUser(String login, String password) {
        int status = 200;
        try {
            Conn();
            stat = conn.createStatement();
            String sql = "INSERT INTO users(login, password) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, login);
            statement.setString(2, password);
            statement.execute();
            statement.close();
            stat.close();
        } catch (PSQLException e) {
            System.out.println(e.getServerErrorMessage()); //добавить определение ошибки повторяющегося логина
            status = 400;
        }
        finally {
            CloseDB();
            return status;
        }
    }

    public static int checkUser(String login, String password) {
        int status = 200;
        try {
            Conn();
            stat = conn.createStatement();
            String sql = "SELECT INTO users(login, password) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, login);
            statement.setString(2, password);
            statement.execute();
            statement.close();
            stat.close();
        } catch (Exception e) {
            System.out.println(e);
            status = 400;
        }
        finally {
            CloseDB();
            return status;
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
