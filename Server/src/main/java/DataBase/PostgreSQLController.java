package DataBase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostgreSQLController {
    private static Connection conn;
    private enum ANSWER_MODE {
        NO_ANSWER,
        ONE_ANSWER,
        NO_OR_ONE_ANSWER,
        MANY_ANSWER
    }
    private enum TYPES {
        STRING,
        INT,
        ARRAY,
        NULL,
        FLOAT
    }

    private Parameter PrmtrOf(Object param, TYPES type) {
        return new Parameter(param, type);
    }
    private class Parameter {
        Object param;
        TYPES type;
        public Parameter(Object param, TYPES type) {
            this.param = param;
            this.type = type;
        }
    }

    private Returning RtrngOf(String name, TYPES type) {
        return new Returning(name, type);
    }
    private class Returning {
        String name;
        TYPES type;
        public Returning(String name, TYPES type) {
            this.name = name;
            this.type = type;
        }
    }

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

    private void putStatement(PreparedStatement statement, Integer i, Object param, TYPES type) throws SQLException {
        if (type == TYPES.STRING) {
            statement.setString(i, (String) param);
        } else if (type == TYPES.INT) {
            statement.setInt(i, (Integer) param);
        } else if (type == TYPES.ARRAY) {
            statement.setArray(i, (Array) param);
        } else if (type == TYPES.FLOAT) {
            statement.setFloat(i, (Float) param);
        } else if (type == TYPES.NULL) {
            statement.setNull(i, Types.NULL);
        }
    }

    private void getAnswer(JSONObject answer, ResultSet resultSet, String name, TYPES type) throws SQLException {
        if (type == TYPES.STRING) {
            answer.put(name, resultSet.getString(name));
        } else if (type == TYPES.INT) {
            answer.put(name, resultSet.getInt(name));
        }
    }

    private void getAnswerArray(JSONObject answer, ResultSet resultSet, String name, TYPES type) throws SQLException {
        JSONArray array = answer.getJSONArray(name);
        if (type == TYPES.STRING) {
            array.put(resultSet.getString(name));
        } else if (type == TYPES.INT) {
            array.put(resultSet.getInt(name));
        }
    }

    private JSONObject doSql(PreparedStatement statement, Parameter[] params, Returning[] answer_infos,
                            ANSWER_MODE answer_mode, JSONObject answer) throws SQLException {
        for (Integer i = 0; i < params.length; i++) {
            putStatement(statement, i + 1, params[i].param, params[i].type);
        }
        if (answer_mode == ANSWER_MODE.NO_ANSWER) {
            statement.execute();
        } else if (answer_mode == ANSWER_MODE.ONE_ANSWER || answer_mode == ANSWER_MODE.NO_OR_ONE_ANSWER) {
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) {
                if (answer_mode == ANSWER_MODE.ONE_ANSWER) {
                    answer.put("error", "no answer from the DataBase");
                }
                statement.close();
                return answer;
            }
            for (Integer i = 0; i < answer_infos.length; i++) {
                getAnswer(answer, resultSet, answer_infos[i].name, answer_infos[i].type);
            }
            if (resultSet.next()) {
                answer.put("error", "too many rows in answer from the DataBase");
                statement.close();
                return answer;
            }
            resultSet.close();
        } else if (answer_mode == ANSWER_MODE.MANY_ANSWER) {
            for (Integer i = 0; i < answer_infos.length; i++) {
                answer.put(answer_infos[i].name, new JSONArray());
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                for (Integer i = 0; i < answer_infos.length; i++) {
                    getAnswerArray(answer, resultSet, answer_infos[i].name, answer_infos[i].type);
                }
            }
            resultSet.close();
        }
        statement.close();
        return answer;
    }

    private Boolean checkForError(JSONObject answer) {
        if (answer.has("error")) {
            System.out.println("DataBase error: " + answer.getString("error"));
            return true;
        }
        return false;
    }
    public JSONObject findShops(String name) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select shop_name as shop_names from users, queues where shop_name like ? and " +
                    "user_id=owner_user_id order by shop_name";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf("%" + name + "%", TYPES.STRING)},
                    new Returning[]{RtrngOf("shop_names", TYPES.STRING)},
                    ANSWER_MODE.MANY_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database findShops: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject findWorker(String login) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select login as logins from users where login like ?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf("%" + login + "%", TYPES.STRING)},
                    new Returning[]{RtrngOf("logins", TYPES.STRING)},
                    ANSWER_MODE.MANY_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database findWorker: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject findQueues(String shop_name) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select queue_name as queue_names, queue_id as queue_ids from queues, users " +
                    "where shop_name=? and owner_user_id=user_id order by number";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(shop_name, TYPES.STRING)},
                    new Returning[]{RtrngOf("queue_names", TYPES.STRING),
                            RtrngOf("queue_ids", TYPES.INT)},
                    ANSWER_MODE.MANY_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database findQueues: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject addUserToQueue(Integer queue_id, Integer user_id, String user_name) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "insert into queue_users(user_id, queue_id, record_name) " +
                    "values (?, ?, ?) returning record_id";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(user_id, user_id == null ? TYPES.NULL : TYPES.INT), PrmtrOf(queue_id, TYPES.INT),
                            PrmtrOf(user_name== null ? "Anonymous" : user_name, TYPES.STRING)},
                    new Returning[]{RtrngOf("record_id", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database addUserToQueue: " + e);
            answer.put("error", e);
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
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queue_id", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "insert into queue_workers(queue_id, worker_user_id) " +
                    "select ?, user_id from users where login = any(?)";
            Array array = conn.createArrayOf("varchar", workers);
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(answer.getInt("queue_id"), TYPES.INT), PrmtrOf(array, TYPES.ARRAY)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database createQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject updateQueue(Integer queue_id, String name, String[] workers) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "update queues set queue_name=? where queue_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(name, TYPES.STRING), PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "select worker_user_id as user_id from queue_workers " +
                    "where queue_id=? and delete_time==null";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(answer.getInt("queue_id"), TYPES.INT)},
                    new Returning[]{RtrngOf("user_id", TYPES.INT)},
                    ANSWER_MODE.MANY_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return info;
            }
            List<Object> old_workers = info.getJSONArray("user_id").toList();
            List<String> workers_list = Arrays.asList(workers);
            List<String> del_workers = new ArrayList<>();
            List<String> new_workers = new ArrayList<>();
            for (Object worker : old_workers) {
                if (!workers_list.contains((String) worker)) {
                    del_workers.add((String) worker);
                }
            }
            for (String worker : workers_list) {
                if (!old_workers.contains(worker)) {
                    new_workers.add(worker);
                }
            }
            sql = "update queue_workers set delete_time=now() where queue_id=? and " +
                    "worker_user_id = any(select user_id from users where login = any(?))";
            Array array = conn.createArrayOf("varchar", del_workers.toArray());
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(answer.getInt("queue_id"), TYPES.INT), PrmtrOf(array, TYPES.ARRAY)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "insert into queue_workers(queue_id, worker_user_id) " +
                    "select ?, user_id from users where login = any(?)";
            array = conn.createArrayOf("varchar", new_workers.toArray());
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(answer.getInt("queue_id"), TYPES.INT), PrmtrOf(array, TYPES.ARRAY)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database createQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject infoQueue(Integer queue_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select worker_user_id as workers from queue_workers " +
                    "where queue_id=? and delete_time==null";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("user_id", TYPES.INT)},
                    ANSWER_MODE.MANY_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "select queue_name from queues where queue_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queue_name", TYPES.STRING)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database createQueue: " + e);
            answer.put("error", e);
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
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT), PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            Integer number = answer.getInt("number");
            answer.put("time", -1);
            /* рпедсказание времени
            sql = "select count(*) as num_req, avg(end_work_time-start_work_time) as mean_time " +
                    "from queue_users where queue_id=? and status='COMPLITED'";
            info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returnings[]{new Returnings("num_req", TYPES.INT), new Returnings("mean_time", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            if (info.getInt("mean_time") < 5) {
                answer.put("time", -1);
            } else {
                answer.put("time", info.getInt("mean_time"));
            }
            */
            sql = "select count(*) as num_workers from queue_workers, history_work " +
                    "where queue_id=? and queue_workers.worker_user_id=history_work.worker_user_id and " +
                    "end_time=null";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("num_workers", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            if (number == 0) {
                sql = "select status, window_name from queue_users, queue_workers " +
                        "where queue_users.record_id=? and queue_users.worker_user_id=queue_workers.worker_user_id";
                doSql(conn.prepareStatement(sql),
                        new Parameter[]{PrmtrOf(record_id, TYPES.INT)},
                        new Returning[]{RtrngOf("status", TYPES.STRING),
                                RtrngOf("window_name", TYPES.STRING)},
                        ANSWER_MODE.ONE_ANSWER, answer);
                checkForError(answer);
            }
        } catch (SQLException e) {
            System.out.println("Database getInfoUserInQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject checkUserStatus(Integer record_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select status from queue_users where record_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{RtrngOf("status", TYPES.STRING)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserStatus: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject rateQueue(Integer record_id, Float rating) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "update queue_users set rating=? where record_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(rating, TYPES.FLOAT), PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database rateQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }
    public JSONObject checkUserInQueue(Integer user_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select record_id, queue_id from queue_users " +
                    "where user_id=? and status='WAIT'";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("record_id", TYPES.INT),
                            RtrngOf("queue_id", TYPES.INT)},
                    ANSWER_MODE.NO_OR_ONE_ANSWER, answer);
            if (checkForError(answer) || !answer.has("queue_id")) {
                CloseDB();
                return answer;
            }
            Integer queue_id = answer.getInt("queue_id");
            sql = "select queue_name from queues queue_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queue_name", TYPES.STRING)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserInQueue: " + e);
            answer.put("error", e);
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
