package com.graphbrain.db;

import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.graphbrain.utils.Permutations.*;


public class MySqlBackend implements Backend {

    private Connection connection;

    private PreparedStatement psGetEdge;
    private PreparedStatement psGetEdgeType;
    private PreparedStatement psGetEntity;
    private PreparedStatement psGetURL;
    private PreparedStatement psGetUser;
    private PreparedStatement psGetProg;
    private PreparedStatement psGetText;

    private PreparedStatement psExistsEdge;
    private PreparedStatement psExistsEdgeType;
    private PreparedStatement psExistsEntity;
    private PreparedStatement psExistsURL;
    private PreparedStatement psExistsUser;
    private PreparedStatement psExistsProg;
    private PreparedStatement psExistsText;

    private PreparedStatement psPutEdge;
    private PreparedStatement psPutEdgeType;
    private PreparedStatement psPutEntity;
    private PreparedStatement psPutURL;
    private PreparedStatement psPutUser;
    private PreparedStatement psPutProg;
    private PreparedStatement psPutText;
    private PreparedStatement psPutEdgePerm;

    private PreparedStatement psUpdateEdge;
    private PreparedStatement psUpdateEdgeType;
    private PreparedStatement psUpdateEntity;
    private PreparedStatement psUpdateURL;
    private PreparedStatement psUpdateUser;
    private PreparedStatement psUpdateProg;
    private PreparedStatement psUpdateText;

    private PreparedStatement psRemoveEdge;
    private PreparedStatement psRemoveEdgeType;
    private PreparedStatement psRemoveEntity;
    private PreparedStatement psRemoveURL;
    private PreparedStatement psRemoveUser;
    private PreparedStatement psRemoveProg;
    private PreparedStatement psRemoveText;
    private PreparedStatement psRemoveEdgePerm;

    private PreparedStatement psEdgeRange;
    private PreparedStatement psUsernameFromEmail;
    private PreparedStatement psAllUsers;

    private PreparedStatement psAddLinkToGlobal;
    private PreparedStatement psRemoveLinkToGlobal;
    private PreparedStatement psGetAlts;

    public MySqlBackend(String name) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String dbConnStr = "jdbc:mysql://localhost/" + name + "?user=gb&password=gb&autoreconnect=true";
            connection = DriverManager.getConnection(dbConnStr);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        createTables();
        createPreparedStatements();
    }

    public MySqlBackend() {
        this("gbnode");
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void safeExec(String sql) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
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

    private void createPreparedStatements() {
        try {
            // get
            psGetEdge = connection.prepareStatement("SELECT degree, ts FROM edges WHERE id=?");
            psGetEdgeType = connection.prepareStatement("SELECT degree, ts, label FROM edgetypes WHERE id=?");
            psGetEntity = connection.prepareStatement("SELECT degree, ts FROM entities WHERE id=?");
            psGetURL = connection.prepareStatement("SELECT degree, ts, title, icon FROM urls WHERE id=?");
            psGetUser = connection.prepareStatement("SELECT degree, ts, username, name, email, pwdhash, role, session, session_ts, last_seen FROM users WHERE id=?");
            psGetProg = connection.prepareStatement("SELECT degree, ts, prog FROM progs WHERE id=?");
            psGetText = connection.prepareStatement("SELECT degree, ts, text FROM texts WHERE id=?");

            // exists
            psExistsEdge = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM edges WHERE id=?)");
            psExistsEdgeType = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM edgetypes WHERE id=?)");
            psExistsEntity = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM entities WHERE id=?)");
            psExistsURL = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM urls WHERE id=?)");
            psExistsUser = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM users WHERE id=?)");
            psExistsProg = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM progs WHERE id=?)");
            psExistsText = connection.prepareStatement("SELECT EXISTS(SELECT 1 FROM texts WHERE id=?)");

            // put
            psPutEdge = connection.prepareStatement("INSERT INTO edges (id, degree, ts) VALUES (?, ?, ?)");
            psPutEdgeType = connection.prepareStatement("INSERT INTO edgetypes (id, degree, ts, label) VALUES (?, ?, ?, ?)");
            psPutEntity = connection.prepareStatement("INSERT INTO entities (id, degree, ts) VALUES (?, ?, ?)");
            psPutURL = connection.prepareStatement("INSERT INTO urls (id, degree, ts, title, icon) VALUES (?, ?, ?, ?, ?)");
            psPutUser = connection.prepareStatement("INSERT INTO users (id, degree, ts, username, name, email, pwdhash, role, session, session_ts, last_seen) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            psPutProg = connection.prepareStatement("INSERT INTO progs (id, degree, ts, prog) VALUES (?, ?, ?, ?)");
            psPutText = connection.prepareStatement("INSERT INTO texts (id, degree, ts, text) VALUES (?, ?, ?, ?)");
            psPutEdgePerm = connection.prepareStatement("INSERT INTO edgeperms (id) VALUES (?)");

            // update
            psUpdateEdge = connection.prepareStatement("UPDATE edges SET degree=?, ts=? WHERE id=?");
            psUpdateEdgeType = connection.prepareStatement("UPDATE edgetypes SET degree=?, ts=?, label=? WHERE id=?");
            psUpdateEntity = connection.prepareStatement("UPDATE entities SET degree=?, ts=? WHERE id=?");
            psUpdateURL = connection.prepareStatement("UPDATE urls SET degree=?, ts=?, title=?, icon=? WHERE id=?");
            psUpdateUser = connection.prepareStatement("UPDATE users SET degree=?, ts=?, username=?, name=?, email=?, pwdhash=?, role=?, session=?, session_ts=?, last_seen=? WHERE id=?");
            psUpdateProg = connection.prepareStatement("UPDATE progs SET degree=?, ts=?, prog=? WHERE id=?");
            psUpdateText = connection.prepareStatement("UPDATE texts SET degree=?, ts=?, text=? WHERE id=?");

            // remove
            psRemoveEdge = connection.prepareStatement("DELETE FROM edges WHERE id=?");
            psRemoveEdgeType = connection.prepareStatement("DELETE FROM edgetypes WHERE id=?");
            psRemoveEntity = connection.prepareStatement("DELETE FROM entities WHERE id=?");
            psRemoveURL = connection.prepareStatement("DELETE FROM urls WHERE id=?");
            psRemoveUser = connection.prepareStatement("DELETE FROM users WHERE id=?");
            psRemoveProg = connection.prepareStatement("DELETE FROM progs WHERE id=?");
            psRemoveText = connection.prepareStatement("DELETE FROM texts WHERE id=?");
            psRemoveEdgePerm = connection.prepareStatement("DELETE FROM edgeperms WHERE id=?");

            // queries
            psEdgeRange = connection.prepareStatement("SELECT id FROM edgeperms WHERE id>=? AND id<?");
            psUsernameFromEmail = connection.prepareStatement("SELECT username FROM users WHERE email=?");
            psAllUsers = connection.prepareStatement("SELECT id, degree, ts, username, name, email, pwdhash, role, session, session_ts, last_seen FROM users");

            // global-user links
            psAddLinkToGlobal = connection.prepareStatement("INSERT INTO globaluser (global_id, user_id) VALUES (?, ?)");
            psRemoveLinkToGlobal = connection.prepareStatement("DELETE FROM globaluser WHERE global_id=? AND user_id=?");
            psGetAlts = connection.prepareStatement("SELECT user_id FROM globaluser WHERE global_id=?");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
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
        try {
            psUsernameFromEmail.setString(1, email);
            ResultSet resultSet = psUsernameFromEmail.executeQuery();

            if (resultSet.next()) {
                return(resultSet.getString("username"));
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    public List<UserNode> allUsers() {
        List<UserNode> res = new LinkedList<>();

        try {
            ResultSet resultSet = psAllUsers.executeQuery();

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
            return null;
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

        try {
            psEdgeRange.setString(1, startStr);
            psEdgeRange.setString(2, endStr);
            ResultSet resultSet = psEdgeRange.executeQuery();

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
            return null;
        }

        return res;
    }

    public Set<Edge> edges(Vertex center) {
        Set<Edge> res = new HashSet<>();

        if (center == null)
            return res;

        String startStr = center.id + " ";
        String endStr = MySqlBackend.strPlusOne(startStr);

        try {
            psEdgeRange.setString(1, startStr);
            psEdgeRange.setString(2, endStr);
            ResultSet resultSet = psEdgeRange.executeQuery();

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
            return null;
        }

        return res;
    }

    private void writeEdgePermutations(Edge edge) {
        //logger.debug(s"writeEdgePermutations: $edge")

        int count = edge.getIds().length;
        int perms = permutations(count);

        for (int i = 0; i < perms; i++) {
            String permId = strArrayPermutationToStr(edge.getIds(), i) + " " + i;
            try {
                psPutEdgePerm.setString(1, permId);
                psPutEdgePerm.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeEdgePermutations(Edge edge) {
        int count = edge.getIds().length;
        int perms = permutations(count);

        for (int i = 0; i < perms; i++) {
            String permId = strArrayPermutationToStr(edge.getIds(), i) + " " + i;
            try {
                psRemoveEdgePerm.setString(1, permId);
                psRemoveEdgePerm.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void addLinkToGlobal(String globalId, String userId) {
        try {
            psAddLinkToGlobal.setString(1, globalId);
            psAddLinkToGlobal.setString(2, userId);

            psAddLinkToGlobal.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeLinkToGlobal(String globalId, String userId) {
        try {
            psRemoveLinkToGlobal.setString(1, globalId);
            psRemoveLinkToGlobal.setString(2, userId);

            psRemoveLinkToGlobal.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<String> alts(String globalId) {
        Set<String> res = new HashSet<>();

        try {
            psGetAlts.setString(1, globalId);
            ResultSet resultSet = psGetAlts.executeQuery();

            while (resultSet.next()) {
                String userId = resultSet.getString("user_id");
                res.add(userId);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return res;
    }

    public static String strPlusOne(String str) {
        char lastChar = str.charAt(str.length() - 1);
        return str.substring(0, str.length() - 1) + ((char)(lastChar + 1));
    }

    private Edge getEdge(String id) {
        try {
            psGetEdge.setString(1, id);
            ResultSet resultSet = psGetEdge.executeQuery();

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                return Edge.fromId(id, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private EdgeType getEdgeType(String id) {
        try {
            psGetEdgeType.setString(1, id);
            ResultSet resultSet = psGetEdgeType.executeQuery();

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String label = resultSet.getString("label");
                return new EdgeType(id, label, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private EntityNode getEntity(String id) {
        try {
            psGetEntity.setString(1, id);
            ResultSet resultSet = psGetEntity.executeQuery();

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                return new EntityNode(id, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private URLNode getURL(String id) {
        try {
            psGetURL.setString(1, id);
            ResultSet resultSet = psGetURL.executeQuery();

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String title = resultSet.getString("title");
                String icon = resultSet.getString("icon");
                return new URLNode(id, title, icon, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private UserNode getUser(String id) {
        try {
            psGetUser.setString(1, id);
            ResultSet resultSet = psGetUser.executeQuery();

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
                return new UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private ProgNode getProg(String id) {
        try {
            psGetProg.setString(1, id);
            ResultSet resultSet = psGetProg.executeQuery();

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String prog = resultSet.getString("prog");
                return new ProgNode(id, prog, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private TextNode getText(String id) {
        try {
            psGetText.setString(1, id);
            ResultSet resultSet = psGetText.executeQuery();

            if (resultSet.next()) {
                int degree = resultSet.getInt("degree");
                long ts = resultSet.getLong("ts");
                String text = resultSet.getString("text");
                return new TextNode(id, text, degree, ts);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            return null;
        }
    }

    private boolean existsEdge(String id) {
        try {
            psExistsEdge.setString(1, id);
            ResultSet resultSet = psExistsEdge.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private boolean existsEdgeType(String id) {
        try {
            psExistsEdgeType.setString(1, id);
            ResultSet resultSet = psExistsEdgeType.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private boolean existsEntity(String id) {
        try {
            psExistsEntity.setString(1, id);
            ResultSet resultSet = psExistsEntity.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private boolean existsURL(String id) {
        try {
            psExistsURL.setString(1, id);
            ResultSet resultSet = psExistsURL.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private boolean existsUser(String id) {
        try {
            psExistsUser.setString(1, id);
            ResultSet resultSet = psExistsUser.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private boolean existsProg(String id) {
        try {
            psExistsProg.setString(1, id);
            ResultSet resultSet = psExistsProg.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private boolean existsText(String id) {
        try {
            psExistsText.setString(1, id);
            ResultSet resultSet = psExistsText.executeQuery();

            return resultSet.next() && resultSet.getInt(1) == 1;
        }
        catch (SQLException e) {
            return false;
        }
    }

    private Edge putEdge(Edge edge) {
        try {
            psPutEdge.setString(1, edge.id);
            psPutEdge.setInt(2, edge.degree);
            psPutEdge.setLong(3, edge.ts);

            psPutEdge.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return edge;
    }

    private EdgeType putEdgeType(EdgeType edgeType) {
        try {
            psPutEdgeType.setString(1, edgeType.id);
            psPutEdgeType.setInt(2, edgeType.degree);
            psPutEdgeType.setLong(3, edgeType.ts);
            psPutEdgeType.setString(4, edgeType.getLabel());

            psPutEdgeType.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return edgeType;
    }

    private EntityNode putEntity(EntityNode entity) {
        try {
            psPutEntity.setString(1, entity.id);
            psPutEntity.setInt(2, entity.degree);
            psPutEntity.setLong(3, entity.ts);

            psPutEntity.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return entity;
    }

    private URLNode putURL(URLNode url) {
        try {
            psPutURL.setString(1, url.id);
            psPutURL.setInt(2, url.degree);
            psPutURL.setLong(3, url.ts);
            psPutURL.setString(4, url.getTitle());
            psPutURL.setString(5, url.getIcon());

            psPutURL.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return url;
    }

    private UserNode putUser(UserNode user) {
        try {
            psPutUser.setString(1, user.id);
            psPutUser.setInt(2, user.degree);
            psPutUser.setLong(3, user.ts);
            psPutUser.setString(4, user.getUsername());
            psPutUser.setString(5, user.getName());
            psPutUser.setString(6, user.getEmail());
            psPutUser.setString(7, user.getPwdhash());
            psPutUser.setString(8, user.getRole());
            psPutUser.setString(9, user.getSession());
            psPutUser.setLong(10, user.getSessionTs());
            psPutUser.setLong(11, user.getLastSeen());

            psPutUser.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return user;
    }

    private ProgNode putProg(ProgNode prog) {
        try {
            psPutProg.setString(1, prog.id);
            psPutProg.setInt(2, prog.degree);
            psPutProg.setLong(3, prog.ts);
            psPutProg.setString(4, prog.getProg());

            psPutProg.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return prog;
    }

    private TextNode putText(TextNode text) {
        try {
            psPutText.setString(1, text.id);
            psPutText.setInt(2, text.degree);
            psPutText.setLong(3, text.ts);
            psPutText.setString(4, text.getText());

            psPutText.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return text;
    }

    private Edge updateEdge(Edge edge) {
        try {
            psUpdateEdge.setInt(1, edge.degree);
            psUpdateEdge.setLong(2, edge.ts);
            psUpdateEdge.setString(3, edge.id);

            psUpdateEdge.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return edge;
    }

    private EdgeType updateEdgeType(EdgeType edgeType) {
        try {
            psUpdateEdgeType.setInt(1, edgeType.degree);
            psUpdateEdgeType.setLong(2, edgeType.ts);
            psUpdateEdgeType.setString(3, edgeType.getLabel());
            psUpdateEdgeType.setString(4, edgeType.id);

            psUpdateEdgeType.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return edgeType;
    }

    private EntityNode updateEntity(EntityNode entity) {
        try {
            psUpdateEntity.setInt(1, entity.degree);
            psUpdateEntity.setLong(2, entity.ts);
            psUpdateEntity.setString(3, entity.id);

            psUpdateEntity.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return entity;
    }

    private URLNode updateURL(URLNode url) {
        try {
            psUpdateURL.setInt(1, url.degree);
            psUpdateURL.setLong(2, url.ts);
            psUpdateURL.setString(3, url.getTitle());
            psUpdateURL.setString(4, url.getIcon());
            psUpdateURL.setString(5, url.id);

            psUpdateURL.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return url;
    }

    private UserNode updateUser(UserNode user) {
        try {
            psUpdateUser.setInt(1, user.degree);
            psUpdateUser.setLong(2, user.ts);
            psUpdateUser.setString(3, user.getUsername());
            psUpdateUser.setString(4, user.getName());
            psUpdateUser.setString(5, user.getEmail());
            psUpdateUser.setString(6, user.getPwdhash());
            psUpdateUser.setString(7, user.getRole());
            psUpdateUser.setString(8, user.getSession());
            psUpdateUser.setLong(9, user.getSessionTs());
            psUpdateUser.setLong(10, user.getLastSeen());
            psUpdateUser.setString(11, user.id);

            psUpdateUser.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return user;
    }

    private ProgNode updateProg(ProgNode prog) {
        try {
            psUpdateProg.setInt(1, prog.degree);
            psUpdateProg.setLong(2, prog.ts);
            psUpdateProg.setString(3, prog.getProg());
            psUpdateProg.setString(4, prog.id);

            psUpdateProg.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return prog;
    }

    private TextNode updateText(TextNode text) {
        try {
            psUpdateText.setInt(1, text.degree);
            psUpdateText.setLong(2, text.ts);
            psUpdateText.setString(3, text.getText());
            psUpdateText.setString(4, text.id);

            psUpdateText.executeUpdate();
        }
        catch (SQLException e) {
            return null;
        }

        return text;
    }

    private void removeEdge(Edge edge) {
        try {
            psRemoveEdge.setString(1, edge.id);
            psRemoveEdge.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeEdgeType(EdgeType edgeType) {
        try {
            psRemoveEdgeType.setString(1, edgeType.id);
            psRemoveEdgeType.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeEntity(EntityNode entity) {
        try {
            psRemoveEntity.setString(1, entity.id);
            psRemoveEntity.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeURL(URLNode urlNode) {
        try {
            psRemoveURL.setString(1, urlNode.id);
            psRemoveURL.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeUser(UserNode user) {
        try {
            psRemoveUser.setString(1, user.id);
            psRemoveUser.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeProg(ProgNode prog) {
        try {
            psRemoveProg.setString(1, prog.id);
            psRemoveProg.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeText(TextNode text) {
        try {
            psRemoveText.setString(1, text.id);
            psRemoveText.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new MySqlBackend();
    }
}