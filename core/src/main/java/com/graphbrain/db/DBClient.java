package com.graphbrain.db;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.graphbrain.db.messages.GetResponse;
import com.graphbrain.db.messages.GetRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

public class DBClient implements Backend {

    Client client;
    SynchronousQueue<Object> queue;

    public DBClient() {
        queue = new SynchronousQueue<>();

        client = new Client();
        Messages.register(client.getKryo());
        client.start();
        try {
            client.connect(5000, "localhost", 54555, 54777);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof GetResponse) {
                    queue.offer(object);
                }
            }
        });
    }

    public void close() {

    }

    public Vertex get(String id, VertexType vtype) {
        client.sendTCP(new GetRequest(id, vtype));

        try {
            Object obj = queue.take();
            GetResponse reply;
            if (obj instanceof GetResponse) {
                reply = (GetResponse)obj;
                return reply.getVertex();
            }
            else {
                return null;
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Vertex put(Vertex vertex) {
        return null;
    }

    public Vertex update(Vertex vertex) {
        return null;
    }

    public void remove(Vertex vertex) {
    }

    public void associateEmailToUsername(String email, String username) {

    }

    public String usernameByEmail(String email) {
        return null;
    }

    public List<Vertex> listByType(VertexType vtype) {
        return null;
    }

    public Set<Edge> edges(Edge pattern) {
        return null;
    }

    public Set<Edge> edges(Vertex center) {
        return null;
    }

    public void addLinkToGlobal(String globalId, String userId) {
    }

    public void removeLinkToGlobal(String globalId, String userId) {
    }

    public Set<String> alts(String globalId) {
        return null;
    }

    public static void main(String[] args) {
        DBServer server = new DBServer();
        server.start();

        DBClient client = new DBClient();
        Vertex v = client.get("1/coimbra", VertexType.Entity);
        System.out.println("reply: " + v.raw() + "; " + v.getTs() + "; " + v.getDegree());
    }
}
