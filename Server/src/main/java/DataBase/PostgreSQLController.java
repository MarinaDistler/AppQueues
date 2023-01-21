package DataBase;

import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import java.lang.reflect.Type;
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

    public JSONObject findShops(String name) {
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
        return new JSONObject().put("shops", shops);
    }

    public JSONObject findQueues(Integer shop_id) {
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
        return new JSONObject().put("queues", queues);
    }

    public JSONObject addUserToQueue(Integer queue_id, Integer user_id, String user_name) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            stat = conn.createStatement();
            String sql = "insert into queue_users(user_id, queue_id, record_name) " +
                    "values (?, ?, ?) returning record_id";
            PreparedStatement statement = conn.prepareStatement(sql);
            if (user_id == null) {
                statement.setNull(1, Types.NULL);
            } else {
                statement.setInt(1, user_id);
            }
            statement.setInt(2, queue_id);
            statement.setString(3,  user_name == null ? "Anonymous" : user_name);
            ResultSet rs = statement.executeQuery();
            rs.next();
            answer.put("record_id", rs.getInt("record_id"));
            rs.close();
            statement.close();
            stat.close();
        } catch (SQLException e) {
            System.out.println("Database addUserToQueue: " + e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject getInfoUserInQueue(Integer record_id, Integer queue_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            stat = conn.createStatement();
            String sql = "select count(*) as number from queue_users " +
                    "where queue_id=? and status='WAIT' and record_id<=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, queue_id);
            statement.setInt(2, record_id);
            ResultSet rs = statement.executeQuery();
            rs.next();
            Integer number = rs.getInt("number");
            answer.put("number", number);
            rs.close();
            statement.close();
            stat.close();
            answer.put("time", -1);
            /* рпедсказание времени
            stat = conn.createStatement();
            sql = "select count(*) as num_req, avg(end_work_time-start_work_time) as mean_time " +
                    "from queue_users where queue_id=? and status='COMPLITED'";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, queue_id);
            rs = statement.executeQuery();
            rs.next();
            if (rs.getInt("mean_time") < 5) {
                answer.put("time", -1);
            } else {
                answer.put("time", rs.getInt("mean_time"));
            }
            rs.close();
            statement.close();
            stat.close();*/
            sql = "select count(*) as num_workers from queue_workers, history_work " +
                    "where queue_id=? and queue_workers.worker_user_id=history_work.worker_user_id and " +
                    "end_time=null";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, queue_id);
            rs = statement.executeQuery();
            rs.next();
            answer.put("num_workers", rs.getInt("num_workers"));
            rs.close();
            statement.close();
            stat.close();
            if (number == 0) {
                sql = "select status, window_name from queue_users, queue_workers " +
                        "where queue_users.record_id=? and queue_users.worker_user_id=queue_workers.worker_user_id";
                statement = conn.prepareStatement(sql);
                statement.setInt(1, record_id);
                rs = statement.executeQuery();
                rs.next();
                answer.put("status", rs.getString("status"));
                answer.put("window_name", rs.getString("window_name"));
                rs.close();
                statement.close();
                stat.close();
            }
        } catch (SQLException e) {
            System.out.println("Database getInfoUserInQueue: " + e);
        }
        CloseDB();
        return answer;
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
