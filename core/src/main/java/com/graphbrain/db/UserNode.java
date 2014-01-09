package com.graphbrain.db;

import org.mindrot.jbcrypt.BCrypt;
import java.math.BigInteger;
import java.util.Map;

import com.graphbrain.utils.RandUtils;


public class UserNode extends Vertex {

    private String username;
    private String name;
    private String email;
    private String pwdhash;
    private String role;
    private String session;
    private long sessionTs;
    private long lastSeen;

    @Override
    public VertexType type() {return VertexType.User;}

    public UserNode(String id,
        String username,
        String name,
        String email,
        String pwdhash,
        String role,
        String session,
        long sessionTs,
        long lastSeen,
        int degree,
        long ts) {

        super(id, degree, ts);
        this.username = username;
        this.name = name;
        this.email = email;
        this.pwdhash = pwdhash;
        this.role = role;
        this.session = session;
        this.sessionTs = sessionTs;
        this.lastSeen = lastSeen;
    }

    public UserNode(String id,
                    String username,
                    String name,
                    String email,
                    String pwdhash,
                    String role) {

        this(id, username, name, email, pwdhash, role, "", -1, -1, 0, -1);
    }

    @Override
    public Vertex copy() {
        return new UserNode(id, username, name, email, pwdhash, role, session, sessionTs, lastSeen, degree, ts);
    }

    public static UserNode create(String username, String name, String email, String password, String role) {
        String id = ID.userIdFromUsername(username);
        String pwdhash = BCrypt.hashpw(password, BCrypt.gensalt());
        return new UserNode(id, username, name, email, pwdhash, role);
    }

    public static UserNode create(String username, String name, String email, String password) {
        return create(username, name, email, password, "");
    }

    public UserNode newSession() {
        session = new BigInteger(130, RandUtils.secRand()).toString(32);
        return this;
    }

    public boolean checkPassword(String candidate) {
        return BCrypt.checkpw(candidate, pwdhash);
    }

    public boolean checkSession(String candidate) {
        return session.equals(candidate);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String raw() {
        return "type: " + "user<br />" + "username: " + username + "<br />"
                + "name: " + name + "<br />" + "role: " + role + "<br />"
                + "lastSeen: " + lastSeen + "<br />";
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPwdhash() {
        return pwdhash;
    }

    public String getSession() {
        return session;
    }

    public String getRole() {
        return role;
    }

    public long getSessionTs() {
        return sessionTs;
    }

    public long getLastSeen() {
        return lastSeen;
    }
}