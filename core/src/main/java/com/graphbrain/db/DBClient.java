package com.graphbrain.db;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.graphbrain.db.messages.*;

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
        Network.registerMessages(client.getKryo());
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
        client.sendTCP(new PutRequest(vertex));

        try {
            Object obj = queue.take();
            if (obj instanceof OK) {
                return vertex;
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

    public Vertex update(Vertex vertex) {
        client.sendTCP(new UpdateRequest(vertex));

        try {
            Object obj = queue.take();
            if (obj instanceof OK) {
                return vertex;
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

    public void remove(Vertex vertex) {
        client.sendTCP(new RemoveRequest(vertex));

        try {
            queue.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void associateEmailToUsername(String email, String username) {
        client.sendTCP(new AssociateEmailToUsernameRequest(email, username));

        try {
            queue.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String usernameByEmail(String email) {
        client.sendTCP(new UsernameByEmailRequest(email));

        try {
            Object obj = queue.take();
            if (obj instanceof UsernameByEmailResponse) {
                return ((UsernameByEmailResponse)obj).getUsername();
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

    public List<Vertex> listByType(VertexType vtype) {
        client.sendTCP(new ListByTypeRequest(vtype));

        try {
            Object obj = queue.take();
            if (obj instanceof ListByTypeResponse) {
                return ((ListByTypeResponse)obj).getVertices();
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

    public Set<Edge> edges(Edge pattern) {
        client.sendTCP(new EdgesRequest(pattern));

        try {
            Object obj = queue.take();
            if (obj instanceof EdgesResponse) {
                return ((EdgesResponse)obj).getVertices();
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

    public Set<Edge> edges(Vertex center) {
        client.sendTCP(new EdgesRequest(center));

        try {
            Object obj = queue.take();
            if (obj instanceof EdgesResponse) {
                return ((EdgesResponse)obj).getVertices();
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

    public void addLinkToGlobal(String globalId, String userId) {
        client.sendTCP(new AddLinkToGlobalRequest(globalId, userId));

        try {
            queue.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeLinkToGlobal(String globalId, String userId) {
        client.sendTCP(new RemoveLinkToGlobalRequest(globalId, userId));

        try {
            queue.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Set<String> alts(String globalId) {
        client.sendTCP(new AltsRequest(globalId));

        try {
            Object obj = queue.take();
            if (obj instanceof AltsResponse) {
                return ((AltsResponse)obj).getAlts();
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

    public static void main(String[] args) {
        DBServer server = new DBServer();
        server.start();

        DBClient client = new DBClient();
        Vertex v = client.get("1/coimbra", VertexType.Entity);
        System.out.println("reply: " + v.raw() + "; " + v.getTs() + "; " + v.getDegree());
    }
}
