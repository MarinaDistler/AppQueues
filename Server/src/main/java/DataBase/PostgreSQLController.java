package DataBase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import javax.swing.plaf.nimbus.State;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostgreSQLController {
    static Connection conn;

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
            String sql = "select shop_name as name, user_id as id from users, queues where shop_name like ? and " +
                    "user_id=owner_user_id order by shop_name";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + name + "%");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                shops.put(rs.getString("name"), rs.getInt("id"));
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Database findShops: " + e);
        }
        CloseDB();
        return new JSONObject().put("shops", shops);
    }

    public JSONObject findWorker(String login) {
        Conn();
        Map<String, Integer> logins = new HashMap<>();
        try {
            String sql = "select login from users where login like ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, "%" + login + "%");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                logins.put(rs.getString("login"), 1);
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Database findWorker: " + e);
        }
        CloseDB();
        return new JSONObject().put("logins", logins);
    }

    public JSONObject findQueues(Integer shop_id) {
        Conn();
        Map<String, Integer> queues = new HashMap<>();
        try {
            String sql = "select queue_name as name, queue_id as id from queues where owner_user_id=? order by number";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, shop_id);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                queues.put(rs.getString("name"), rs.getInt("id"));
            }
            rs.close();
            statement.close();
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
        } catch (SQLException e) {
            System.out.println("Database addUserToQueue: " + e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject createQueue(String name, Integer user_id, String[] workers) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "insert into queues(queue_name, owner_user_id) " +
                    "values (?, ?) returning queue_id";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1,  name);
            statement.setInt(2, user_id);
            ResultSet rs = statement.executeQuery();
            rs.next();
            Integer queue_id =  rs.getInt("queue_id");
            rs.close();
            statement.close();
            sql = "insert into queue_workers(queue_id, worker_user_id) " +
                    "select ?, user_id from users where login = any(?)";
            statement = conn.prepareStatement(sql);
            statement.setInt(1,  queue_id);
            Array array = conn.createArrayOf("varchar", workers);
            statement.setArray(2, array);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Database createQueue: " + e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject getInfoUserInQueue(Integer record_id, Integer queue_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
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
            }
        } catch (SQLException e) {
            System.out.println("Database getInfoUserInQueue: " + e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject checkUserStatus(Integer record_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select status from queue_users where record_id=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, record_id);
            ResultSet rs = statement.executeQuery();
            rs.next();
            answer.put("status", rs.getString("status"));
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Database checkUserStatus: " + e);
        }
        CloseDB();
        return answer;
    }

    public void rateQueue(Integer record_id, Float rating) {
        Conn();
        try {
            String sql = "update queue_users set rating=? where record_id=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setFloat(1, rating);
            statement.setInt(2, record_id);
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Database rateQueue: " + e);
        }
        CloseDB();
    }
    public JSONObject checkUserInQueue(Integer user_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select record_id, queue_id, queue_name from queue_users " +
                    "where user_id=? and status='WAIT'";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                answer.put("record_id", rs.getInt("record_id"));
                answer.put("queue_id", rs.getInt("queue_id"));
                answer.put("queue", rs.getString("queue_name"));
                if (rs.next()) {
                    System.out.println("DataBase checkUserInQueue error: too many records for one user");
                }
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            System.out.println("Database getInfoUserInQueue: " + e);
        }
        CloseDB();
        return answer;
    }

    public static int addUser(String login, String password) {
        int user_id = 0;
        try {
            Conn();
            String sql = "INSERT INTO users(login, password) VALUES (?, ?) RETURNING user_id";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            rs.next();
            user_id =  rs.getInt("user_id");
            rs.close();
            statement.close();
        } catch (PSQLException e) {
            System.out.println(e.getServerErrorMessage()); //добавить определение ошибки повторяющегося логина
        }
        finally {
            CloseDB();
            return user_id;
        }
    }

    public static int checkUser(String login, String password) {
        int user_id = 0;
        try {
            Conn();
            String sql = "SELECT user_id from users where login=? and password=?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                user_id = rs.getInt("user_id");
            }
            rs.close();
            statement.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        finally {
            CloseDB();
            return user_id;
        }
    }

    public static ArrayList<String> getAllNames() throws ClassNotFoundException, SQLException
    {
        ArrayList<String> names = new ArrayList<String>();

        Conn();
        Statement stat = conn.createStatement();
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
