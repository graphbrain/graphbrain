package com.graphbrain.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.graphbrain.utils.Permutations.*;


public class MySqlBackend implements Backend {

    private HikariDataSource ds;

    public MySqlBackend(String name) {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(100);
        config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        config.addDataSourceProperty("url", "jdbc:mysql://localhost/" + name);
        config.addDataSourceProperty("user", "gb");
        config.addDataSourceProperty("password", "gb");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        ds = new HikariDataSource(config);

        createTables();
    }

    public MySqlBackend() {
        this("gbnode");
    }

    public void close() {}

    private Connection getConnection() {
        try {
            return ds.getConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void closeCSR(Connection conn, PreparedStatement stat, ResultSet rs) {
        if (stat != null) {
            try {
                stat.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            try {
                conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void safeExec(String sql) {
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = getConnection();
            statement = conn.prepareStatement(sql);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, statement, null);
        }
    }

    private void createTables() {
        String MYSQL_ENGINE = "InnoDB";
        //String MYSQL_ENGINE = "MyISAM";

        // Edges table
        String createTableStr = "CREATE TABLE IF NOT EXISTS edges (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // EdgeTypes table
        createTableStr = "CREATE TABLE IF NOT EXISTS edgetypes (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "label VARCHAR(255),";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // Entities table
        createTableStr = "CREATE TABLE IF NOT EXISTS entities (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // URLs table
        createTableStr = "CREATE TABLE IF NOT EXISTS urls (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "title VARCHAR(500),";
        createTableStr += "icon VARCHAR(500),";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // Users table
        createTableStr = "CREATE TABLE IF NOT EXISTS users (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "username VARCHAR(255),";
        createTableStr += "name VARCHAR(255),";
        createTableStr += "email VARCHAR(255),";
        createTableStr += "pwdhash VARCHAR(255),";
        createTableStr += "role VARCHAR(255),";
        createTableStr += "session VARCHAR(255),";
        createTableStr += "session_ts BIGINT DEFAULT -1,";
        createTableStr += "last_seen BIGINT DEFAULT -1,";
        createTableStr += "INDEX id_index (id(255)),";
        createTableStr += "INDEX email_index (email)";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // Progs table
        createTableStr = "CREATE TABLE IF NOT EXISTS progs (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "prog TEXT,";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // Texts table
        createTableStr = "CREATE TABLE IF NOT EXISTS texts (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "degree INT DEFAULT 0,";
        createTableStr += "ts BIGINT DEFAULT -1,";
        createTableStr += "text TEXT,";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // Edge permutations table
        createTableStr = "CREATE TABLE IF NOT EXISTS edgeperms (";
        createTableStr += "id VARCHAR(10000),";
        createTableStr += "INDEX id_index (id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);

        // Global-User table
        createTableStr = "CREATE TABLE IF NOT EXISTS globaluser (";
        createTableStr += "global_id VARCHAR(10000),";
        createTableStr += "user_id VARCHAR(10000),";
        createTableStr += "INDEX global_id_index (global_id(255))";
        createTableStr += ") ENGINE=" + MYSQL_ENGINE + " DEFAULT CHARSET=utf8;";
        safeExec(createTableStr);
    }

    public Vertex get(String id, VertexType vtype) {
        switch(vtype) {
            case Edge:
                return getEdge(id);
            case EdgeType:
                return getEdgeType(id);
            case Entity:
                return getEntity(id);
            case URL:
                return getURL(id);
            case User:
                return getUser(id);
            case Prog:
                return getProg(id);
            case Text:
                return getText(id);
            default:
                return null;
        }
    }

    public boolean exists(String id, VertexType vtype) {
        switch(vtype) {
            case Edge:
                return existsEdge(id);
            case EdgeType:
                return existsEdgeType(id);
            case Entity:
                return existsEntity(id);
            case URL:
                return existsURL(id);
            case User:
                return existsUser(id);
            case Prog:
                return existsProg(id);
            case Text:
                return existsText(id);
            default:
                return false;
        }
    }

    public Vertex put(Vertex vertex) {
        switch(vertex.type()) {
            case Edge:
                Edge edge = putEdge((Edge)vertex);
                if (vertex.type() == VertexType.Edge) {
                    Edge e = (Edge)vertex;
                    writeEdgePermutations(e);
                }
                return edge;
            case EdgeType:
                return putEdgeType((EdgeType)vertex);
            case Entity:
                return putEntity((EntityNode)vertex);
            case URL:
                return putURL((URLNode)vertex);
            case User:
                return putUser((UserNode)vertex);
            case Prog:
                return putProg((ProgNode)vertex);
            case Text:
                return putText((TextNode)vertex);
            default:
                return null;
        }
    }

    public Vertex update(Vertex vertex) {
        switch(vertex.type()) {
            case Edge:
                return updateEdge((Edge) vertex);
            case EdgeType:
                return updateEdgeType((EdgeType) vertex);
            case Entity:
                return updateEntity((EntityNode) vertex);
            case URL:
                return updateURL((URLNode) vertex);
            case User:
                return updateUser((UserNode) vertex);
            case Prog:
                return updateProg((ProgNode) vertex);
            case Text:
                return updateText((TextNode) vertex);
            default:
                return null;
        }
    }

    public void remove(Vertex vertex) {
        switch(vertex.type()) {
            case Edge:
                Edge e = (Edge)vertex;
                removeEdge(e);
                removeEdgePermutations(e);
                break;
            case EdgeType:
                removeEdgeType((EdgeType) vertex);
                break;
            case Entity:
                removeEntity((EntityNode) vertex);
                break;
            case URL:
                removeURL((URLNode) vertex);
                break;
            case User:
                removeUser((UserNode) vertex);
                break;
            case Prog:
                removeProg((ProgNode) vertex);
                break;
            case Text:
                removeText((TextNode) vertex);
                break;
            default:
                break;
        }
    }

    public void associateEmailToUsername(String email, String username) {}

    public String usernameByEmail(String email) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT username FROM users WHERE email=?");
            ps.setString(1, email);
            resultSet = ps.executeQuery();

            String res = null;
            if (resultSet.next()) {
                res = resultSet.getString("username");
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    public List<UserNode> allUsers() {
        List<UserNode> res = new LinkedList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT id, degree, ts, username, name, email, pwdhash, role, session, session_ts, last_seen FROM users");
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String pwdhash = resultSet.getString("pwdhash");
                String role = resultSet.getString("role");
                String session = resultSet.getString("session");
                long sessionTs = resultSet.getLong("session_ts");
                long lastSeen = resultSet.getLong("last_seen");
                UserNode user = new UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, degree, ts);
                res.add(user);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }

        return res;
    }

    public Set<Edge> edges(Edge pattern) {
        Set<Edge> res = new HashSet<>();

        StringBuilder sb = new StringBuilder(100);

        boolean first = true;
        for (String p : pattern.getIds()) {
            if (!p.equals("*")) {
                if (first)
                    first = false;
                else
                    sb.append(" ");
                sb.append(p);
            }
        }

        String startStr = sb.toString();
        String endStr = MySqlBackend.strPlusOne(startStr);

        Connection conn = null;
        PreparedStatement psEdgeRange = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            psEdgeRange = conn.prepareStatement("SELECT id FROM edgeperms WHERE id>=? AND id<?");
            psEdgeRange.setString(1, startStr);
            psEdgeRange.setString(2, endStr);
            resultSet = psEdgeRange.executeQuery();

            while (resultSet.next()) {
                String pid = resultSet.getString("id");
                String[] tokens = pid.split(" ");
                int perm = Integer.parseInt(tokens[tokens.length - 1]);
                tokens = Arrays.copyOfRange(tokens, 0, tokens.length - 1);
                tokens = strArrayUnpermutate(tokens, perm);
                Edge edge = Edge.fromParticipants(tokens);
                if (edge.matches(pattern))
                    res.add(edge);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, psEdgeRange, resultSet);
        }

        return res;
    }

    public Set<Edge> edges(Vertex center) {
        Set<Edge> res = new HashSet<>();

        if (center == null)
            return res;

        String startStr = center.id + " ";
        String endStr = MySqlBackend.strPlusOne(startStr);

        Connection conn = null;
        PreparedStatement psEdgeRange = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            psEdgeRange = conn.prepareStatement("SELECT id FROM edgeperms WHERE id>=? AND id<?");
            psEdgeRange.setString(1, startStr);
            psEdgeRange.setString(2, endStr);
            resultSet = psEdgeRange.executeQuery();

            while (resultSet.next()) {
                String pid = resultSet.getString("id");
                String[] tokens = pid.split(" ");
                int perm = Integer.parseInt(tokens[tokens.length - 1]);
                tokens = Arrays.copyOfRange(tokens, 0, tokens.length - 1);
                tokens = strArrayUnpermutate(tokens, perm);
                Edge edge = Edge.fromParticipants(tokens);
                res.add(edge);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, psEdgeRange, resultSet);
        }

        return res;
    }

    private void writeEdgePermutations(Edge edge) {
        //logger.debug(s"writeEdgePermutations: $edge")

        int count = edge.getIds().length;
        int perms = permutations(count);

        for (int i = 0; i < perms; i++) {
            String permId = strArrayPermutationToStr(edge.getIds(), i) + " " + i;

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = getConnection();
                ps = conn.prepareStatement("INSERT INTO edgeperms (id) VALUES (?)");
                ps.setString(1, permId);
                ps.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                closeCSR(conn, ps, null);
            }
        }
    }

    private void removeEdgePermutations(Edge edge) {
        int count = edge.getIds().length;
        int perms = permutations(count);

        for (int i = 0; i < perms; i++) {
            String permId = strArrayPermutationToStr(edge.getIds(), i) + " " + i;

            Connection conn = null;
            PreparedStatement ps = null;
            try {
                conn = getConnection();
                ps = conn.prepareStatement("DELETE FROM edgeperms WHERE id=?");
                ps.setString(1, permId);
                ps.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
            finally {
                closeCSR(conn, ps, null);
            }
        }
    }

    public void addLinkToGlobal(String globalId, String userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO globaluser (global_id, user_id) VALUES (?, ?)");
            ps.setString(1, globalId);
            ps.setString(2, userId);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    public void removeLinkToGlobal(String globalId, String userId) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM globaluser WHERE global_id=? AND user_id=?");
            ps.setString(1, globalId);
            ps.setString(2, userId);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    public Set<String> alts(String globalId) {
        Set<String> res = new HashSet<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT user_id FROM globaluser WHERE global_id=?");
            ps.setString(1, globalId);
            resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                res.add(userId);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }

        return res;
    }

    public static String strPlusOne(String str) {
        char lastChar = str.charAt(str.length() - 1);
        return str.substring(0, str.length() - 1) + ((char)(lastChar + 1));
    }

    private Edge getEdge(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT degree, ts FROM edges WHERE id=?");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            Edge res = null;

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                res = Edge.fromId(id, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private EdgeType getEdgeType(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT degree, ts, label FROM edgetypes WHERE id=?");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            EdgeType res = null;
            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String label = resultSet.getString("label");
                res = new EdgeType(id, label, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private EntityNode getEntity(String id) {
        Connection conn = null;
        PreparedStatement psGetEntity = null;
        ResultSet resultSet = null;

        try {
            conn = getConnection();
            psGetEntity = conn.prepareStatement("SELECT degree, ts FROM entities WHERE id=?");
            psGetEntity.setString(1, id);
            resultSet = psGetEntity.executeQuery();

            EntityNode res = null;

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                res = new EntityNode(id, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, psGetEntity, resultSet);
        }
    }

    private URLNode getURL(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT degree, ts, title, icon FROM urls WHERE id=?");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            URLNode res = null;

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String title = resultSet.getString("title");
                String icon = resultSet.getString("icon");
                res = new URLNode(id, title, icon, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private UserNode getUser(String id) {
        Connection conn = null;
        PreparedStatement psGetUser = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            psGetUser = conn.prepareStatement("SELECT degree, ts, username, name, email, pwdhash, role, session, session_ts, last_seen FROM users WHERE id=?");
            psGetUser.setString(1, id);
            resultSet = psGetUser.executeQuery();

            UserNode res = null;

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String username = resultSet.getString("username");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String pwdhash = resultSet.getString("pwdhash");
                String role = resultSet.getString("role");
                String session = resultSet.getString("session");
                long sessionTs = resultSet.getLong("session_ts");
                long lastSeen = resultSet.getLong("last_seen");
                res = new UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, psGetUser, resultSet);
        }
    }

    private ProgNode getProg(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT degree, ts, prog FROM progs WHERE id=?");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            ProgNode res = null;

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String prog = resultSet.getString("prog");
                res = new ProgNode(id, prog, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private TextNode getText(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT degree, ts, text FROM texts WHERE id=?");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            TextNode res = null;

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String text = resultSet.getString("text");
                res = new TextNode(id, text, degree, ts);
            }

            return res;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private boolean existsEdge(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM edges WHERE id=?)");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private boolean existsEdgeType(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM edgetypes WHERE id=?)");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private boolean existsEntity(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM entities WHERE id=?)");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private boolean existsURL(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM urls WHERE id=?)");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private boolean existsUser(String id) {
        Connection conn = null;
        PreparedStatement psExistsUser = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            psExistsUser = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE id=?)");
            psExistsUser.setString(1, id);
            resultSet = psExistsUser.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, psExistsUser, resultSet);
        }
    }

    private boolean existsProg(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM progs WHERE id=?)");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private boolean existsText(String id) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("SELECT EXISTS(SELECT 1 FROM texts WHERE id=?)");
            ps.setString(1, id);
            resultSet = ps.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            closeCSR(conn, ps, resultSet);
        }
    }

    private Edge putEdge(Edge edge) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO edges (id, degree, ts) VALUES (?, ?, ?)");
            ps.setString(1, edge.id);
            ps.setInt(2, edge.degree);
            ps.setLong(3, edge.ts);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return edge;
    }

    private EdgeType putEdgeType(EdgeType edgeType) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO edgetypes (id, degree, ts, label) VALUES (?, ?, ?, ?)");
            ps.setString(1, edgeType.id);
            ps.setInt(2, edgeType.degree);
            ps.setLong(3, edgeType.ts);
            ps.setString(4, edgeType.getLabel());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return edgeType;
    }

    private EntityNode putEntity(EntityNode entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO entities (id, degree, ts) VALUES (?, ?, ?)");
            ps.setString(1, entity.id);
            ps.setInt(2, entity.degree);
            ps.setLong(3, entity.ts);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return entity;
    }

    private URLNode putURL(URLNode url) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO urls (id, degree, ts, title, icon) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, url.id);
            ps.setInt(2, url.degree);
            ps.setLong(3, url.ts);
            ps.setString(4, url.getTitle());
            ps.setString(5, url.getIcon());

            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return url;
    }

    private UserNode putUser(UserNode user) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO users (id, degree, ts, username, name, email, pwdhash, role, session, session_ts, last_seen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, user.id);
            ps.setInt(2, user.degree);
            ps.setLong(3, user.ts);
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getName());
            ps.setString(6, user.getEmail());
            ps.setString(7, user.getPwdhash());
            ps.setString(8, user.getRole());
            ps.setString(9, user.getSession());
            ps.setLong(10, user.getSessionTs());
            ps.setLong(11, user.getLastSeen());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return user;
    }

    private ProgNode putProg(ProgNode prog) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO progs (id, degree, ts, prog) VALUES (?, ?, ?, ?)");
            ps.setString(1, prog.id);
            ps.setInt(2, prog.degree);
            ps.setLong(3, prog.ts);
            ps.setString(4, prog.getProg());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return prog;
    }

    private TextNode putText(TextNode text) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("INSERT INTO texts (id, degree, ts, text) VALUES (?, ?, ?, ?)");
            ps.setString(1, text.id);
            ps.setInt(2, text.degree);
            ps.setLong(3, text.ts);
            ps.setString(4, text.getText());

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return text;
    }

    private Edge updateEdge(Edge edge) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE edges SET degree=?, ts=? WHERE id=?");
            ps.setInt(1, edge.degree);
            ps.setLong(2, edge.ts);
            ps.setString(3, edge.id);

            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return edge;
    }

    private EdgeType updateEdgeType(EdgeType edgeType) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE edgetypes SET degree=?, ts=?, label=? WHERE id=?");
            ps.setInt(1, edgeType.degree);
            ps.setLong(2, edgeType.ts);
            ps.setString(3, edgeType.getLabel());
            ps.setString(4, edgeType.id);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return edgeType;
    }

    private EntityNode updateEntity(EntityNode entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE entities SET degree=?, ts=? WHERE id=?");
            ps.setInt(1, entity.degree);
            ps.setLong(2, entity.ts);
            ps.setString(3, entity.id);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return entity;
    }

    private URLNode updateURL(URLNode url) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE urls SET degree=?, ts=?, title=?, icon=? WHERE id=?");
            ps.setInt(1, url.degree);
            ps.setLong(2, url.ts);
            ps.setString(3, url.getTitle());
            ps.setString(4, url.getIcon());
            ps.setString(5, url.id);

            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return url;
    }

    private UserNode updateUser(UserNode user) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE users SET degree=?, ts=?, username=?, name=?, email=?, pwdhash=?, role=?, session=?, session_ts=?, last_seen=? WHERE id=?");
            ps.setInt(1, user.degree);
            ps.setLong(2, user.ts);
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getName());
            ps.setString(5, user.getEmail());
            ps.setString(6, user.getPwdhash());
            ps.setString(7, user.getRole());
            ps.setString(8, user.getSession());
            ps.setLong(9, user.getSessionTs());
            ps.setLong(10, user.getLastSeen());
            ps.setString(11, user.id);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return user;
    }

    private ProgNode updateProg(ProgNode prog) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE progs SET degree=?, ts=?, prog=? WHERE id=?");
            ps.setInt(1, prog.degree);
            ps.setLong(2, prog.ts);
            ps.setString(3, prog.getProg());
            ps.setString(4, prog.id);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return prog;
    }

    private TextNode updateText(TextNode text) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("UPDATE texts SET degree=?, ts=?, text=? WHERE id=?");
            ps.setInt(1, text.degree);
            ps.setLong(2, text.ts);
            ps.setString(3, text.getText());
            ps.setString(4, text.id);

            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            closeCSR(conn, ps, null);
        }

        return text;
    }

    private void removeEdge(Edge edge) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM edges WHERE id=?");
            ps.setString(1, edge.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    private void removeEdgeType(EdgeType edgeType) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM edgetypes WHERE id=?");
            ps.setString(1, edgeType.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    private void removeEntity(EntityNode entity) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM entities WHERE id=?");
            ps.setString(1, entity.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    private void removeURL(URLNode urlNode) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM urls WHERE id=?");
            ps.setString(1, urlNode.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    private void removeUser(UserNode user) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM users WHERE id=?");
            ps.setString(1, user.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    private void removeProg(ProgNode prog) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM progs WHERE id=?");
            ps.setString(1, prog.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    private void removeText(TextNode text) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement("DELETE FROM texts WHERE id=?");
            ps.setString(1, text.id);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeCSR(conn, ps, null);
        }
    }

    public static void main(String[] args) {
        new MySqlBackend();
    }
}