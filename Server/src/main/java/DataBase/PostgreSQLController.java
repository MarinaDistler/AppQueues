package DataBase;

import org.json.JSONArray;
import org.json.JSONObject;
import org.postgresql.util.PGInterval;

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
        FLOAT,
        TIMESTAMP,
        INTERVAL
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
        } else if (type == TYPES.TIMESTAMP) {
            statement.setTimestamp(i, (Timestamp) param);
        }
    }

    private void getAnswer(JSONObject answer, ResultSet resultSet, String name, TYPES type) throws SQLException {
        if (type == TYPES.STRING) {
            answer.put(name, resultSet.getString(name));
        } else if (type == TYPES.INT) {
            answer.put(name, resultSet.getInt(name));
        } else if (type == TYPES.ARRAY) {
            answer.put(name, resultSet.getArray(name));
        } else if (type == TYPES.FLOAT) {
            answer.put(name, resultSet.getFloat(name));
        } else if (type == TYPES.TIMESTAMP) {
            answer.put(name, resultSet.getTimestamp(name));
        } else if (type == TYPES.INTERVAL) {
            PGInterval dur = (PGInterval) resultSet.getObject(name);
            if (dur == null || dur.getDays() > 0) {
                answer.put(name, -1);
            } else {
                answer.put(name, dur.getHours() * 3600 + dur.getMinutes() * 60 + dur.getSeconds());
            }
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
                resultSet.close();
                statement.close();
                return answer;
            }
            for (Integer i = 0; i < answer_infos.length; i++) {
                getAnswer(answer, resultSet, answer_infos[i].name, answer_infos[i].type);
            }
            if (resultSet.next()) {
                answer.put("error", "too many rows in answer from the DataBase");
                resultSet.close();
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
            String sql = "select shop_name as shop_names from users where shop_name like ? " +
                    "order by shop_name";
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

    public JSONObject viewAllQueues(Integer user_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select queue_name as queues from queues where " +
                    "owner_user_id=? and delete_time is null";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queues", TYPES.STRING)},
                    ANSWER_MODE.MANY_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database viewAllQueues: " + e);
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
                    "where shop_name=? and owner_user_id=user_id and delete_time is null order by number";
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

    public JSONObject exitUserFromQueue(Integer record_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "update queue_users set status='CANCELED' where record_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database addUserToQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject skipPlaceUserInQueue(Integer record_id, Integer queue_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select number from queue_users where record_id=?";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return answer;
            }
            Integer my_number = info.getInt("number");
            sql = "select record_id, number from queue_users where queue_id=? and status='WAIT' and number>? " +
                    "order by number asc limit 1";
            info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT), PrmtrOf(my_number, TYPES.INT)},
                    new Returning[]{RtrngOf("record_id", TYPES.INT), RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.NO_OR_ONE_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return answer;
            }
            if (!info.has("record_id") & !info.has("number")) {
                answer.put("notification", "You are in the last place in the queue");
                CloseDB();
                return answer;
            }
            sql = "update queue_users set number=? where record_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(my_number, TYPES.INT), PrmtrOf(info.getInt("record_id"), TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(info.getInt("number"), TYPES.INT), PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
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
            String sql = "select count(*) as number from queues where queue_name=? and " +
                    "owner_user_id=? and delete_time is null";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return info;
            }
            if (info.getInt("number") > 0) {
                CloseDB();
                return answer.put("error", "You already have queue with this name");
            }
            sql = "insert into queues(queue_name, owner_user_id) " +
                    "values (?, ?) returning queue_id";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queue_id", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            Integer queue_id = answer.getInt("queue_id");
            sql = "insert into queue_workers(queue_id, worker_user_id) " +
                    "select ?, user_id from users where login = any(?)";
            Array array = conn.createArrayOf("varchar", workers);
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT), PrmtrOf(array, TYPES.ARRAY)},
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

    public JSONObject updateQueue(String old_name, Integer user_id, String name, String[] workers) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select queue_id from queues " +
                    "where queue_name=? and owner_user_id=? and delete_time is null";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(old_name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queue_id", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return info;
            }
            Integer queue_id = info.getInt("queue_id");
            sql = "update queues set queue_name=? where queue_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(name, TYPES.STRING), PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "select login as logins from queue_workers, users " +
                    "where queue_id=? and delete_time is null and worker_user_id=user_id ";
            info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("logins", TYPES.STRING)},
                    ANSWER_MODE.MANY_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return info;
            }
            List<Object> old_workers = info.getJSONArray("logins").toList();
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
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT), PrmtrOf(array, TYPES.ARRAY)},
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
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT), PrmtrOf(array, TYPES.ARRAY)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database updateQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject deleteQueue(String queue_name, Integer user_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select queue_id from queues " +
                    "where queue_name=? and owner_user_id=? and delete_time is null";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("queue_id", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return info;
            }
            Integer queue_id = info.getInt("queue_id");
            sql = "update queues set delete_time=now() where queue_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "update queue_workers set delete_time=now() where queue_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database deleteQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject infoQueue(String queue_name, Integer user_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select login as workers from queue_workers, queues, users " +
                    "where queue_name=? and owner_user_id=? and user_id=worker_user_id and " +
                    "queues.queue_id=queue_workers.queue_id and queue_workers.delete_time is null";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("workers", TYPES.STRING)},
                    ANSWER_MODE.MANY_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database infoQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject getInfoUserInQueue(Integer record_id, Integer queue_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select number from queue_users where record_id=?";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            Integer number = info.getInt("number");
            sql = "select count(*) as number from queue_users " +
                    "where queue_id=? and status='WAIT' and number<=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT), PrmtrOf(number, TYPES.INT)},
                    new Returning[]{RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            sql = "select percentile_cont(0.5) within group (order by end_work_time-start_work_time) as median_time " +
                    "from queue_users where queue_id=? and status='COMPLETED' and " +
                    "current_timestamp-start_work_time<=interval '1 day'";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("median_time", TYPES.INTERVAL)},
                    ANSWER_MODE.ONE_ANSWER, info);
            if (checkForError(info)) {
                CloseDB();
                return answer;
            }
            sql = "select count(*) as num_workers from queue_workers, history_work " +
                    "where queue_id=? and queue_workers.worker_user_id=history_work.worker_user_id and " +
                    "end_time is null and delete_time is null";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(queue_id, TYPES.INT)},
                    new Returning[]{RtrngOf("num_workers", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (checkForError(answer)) {
                CloseDB();
                return answer;
            }
            Integer median_time = info.getInt("median_time");
            if (median_time == -1) {
                answer.put("time", -1);
            } else {
                Integer num_workers = answer.getInt("num_workers");
                if (num_workers != 0) {
                    answer.put("time", (median_time * (answer.getInt("number") - 1) + 90 * answer.getInt("number")) / num_workers);
                } else {
                    answer.put("time", -1);
                }
            }
            sql = "select status, window_name from queue_users, queue_workers " +
                    "where queue_users.record_id=? and queue_users.worker_user_id=queue_workers.worker_user_id " +
                    "and queue_workers.queue_id=queue_users.queue_id";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(record_id, TYPES.INT)},
                    new Returning[]{RtrngOf("status", TYPES.STRING),
                            RtrngOf("window_name", TYPES.STRING)},
                    ANSWER_MODE.NO_OR_ONE_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database getInfoUserInQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject getProfile(Integer user_id) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select shop_name, login from users where user_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("shop_name", TYPES.STRING), RtrngOf("login", TYPES.STRING)},
                    ANSWER_MODE.ONE_ANSWER, answer);
            if (!answer.has("shop_name")) {
                answer.put("shop_name", "");
            }
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserStatus: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject updateUserLogin(Integer user_id, String login) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "update users set login=? where user_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(login, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserStatus: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject updateUserShopName(Integer user_id, String shop_name) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "update users set shop_name=? where user_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(shop_name, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserStatus: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject updateUserPassword(Integer user_id, String password) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "update users set password=? where user_id=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(password, TYPES.STRING), PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{},
                    ANSWER_MODE.NO_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserStatus: " + e);
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
            String sql = "select record_id, queues.queue_id as queue_id, queue_name, shop_name " +
                    "from queue_users, queues join users on owner_user_id=users.user_id " +
                    "where queue_users.user_id=? and (status='WAIT' or status='WORK') " +
                    "and queue_users.queue_id=queues.queue_id";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(user_id, TYPES.INT)},
                    new Returning[]{RtrngOf("record_id", TYPES.INT),
                            RtrngOf("queue_id", TYPES.INT), RtrngOf("queue_name", TYPES.STRING),
                            RtrngOf("shop_name", TYPES.STRING) },
                    ANSWER_MODE.NO_OR_ONE_ANSWER, answer);
            checkForError(answer);
        } catch (SQLException e) {
            System.out.println("Database checkUserInQueue: " + e);
            answer.put("error", e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject createUser(String login, String password, String shop_name) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select count(*) as number from users where login=?";
            JSONObject info = doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(login, TYPES.STRING)},
                    new Returning[]{RtrngOf("number", TYPES.INT)},
                    ANSWER_MODE.ONE_ANSWER, new JSONObject());
            if (checkForError(info)) {
                CloseDB();
                return answer;
            }
            if (info.getInt("number") != 0) {
                answer.put("notification", "Login already exists!");
                CloseDB();
                return answer;
            }
            if (shop_name != null) {
                sql = "select count(*) as number from users where shop_name=?";
                info = doSql(conn.prepareStatement(sql),
                        new Parameter[]{PrmtrOf(shop_name, TYPES.STRING)},
                        new Returning[]{RtrngOf("number", TYPES.INT)},
                        ANSWER_MODE.ONE_ANSWER, new JSONObject());
                if (checkForError(info)) {
                    CloseDB();
                    return answer;
                }
                if (info.getInt("number") != 0) {
                    answer.put("notification", "Shop name already exists!");
                    CloseDB();
                    return answer;
                }
            }
            sql = "insert into users(login, password, shop_name) values (?, ?, ?) returning user_id";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(login, TYPES.STRING), PrmtrOf(password, TYPES.STRING),
                            PrmtrOf(shop_name, TYPES.STRING)},
                    new Returning[]{RtrngOf("user_id", TYPES.INT)},
                    ANSWER_MODE.NO_OR_ONE_ANSWER, answer);
            checkForError(answer);
        } catch (Exception e) {
            System.out.println("Database createUser: " + e);
            System.out.println(e);
        }
        CloseDB();
        return answer;
    }

    public JSONObject loginUser(String login, String password) {
        Conn();
        JSONObject answer = new JSONObject();
        try {
            String sql = "select user_id from users where login=? and password=?";
            doSql(conn.prepareStatement(sql),
                    new Parameter[]{PrmtrOf(login, TYPES.STRING), PrmtrOf(password, TYPES.STRING)},
                    new Returning[]{RtrngOf("user_id", TYPES.INT)},
                    ANSWER_MODE.NO_OR_ONE_ANSWER, answer);
            checkForError(answer);
        } catch (Exception e) {
            System.out.println("Database loginUser: " + e);
            System.out.println(e);
        }
        CloseDB();
        return answer;
    }

}
