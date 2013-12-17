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

    DBServer server;
    Client client;
    SynchronousQueue<Object> queue;

    public DBClient(String name) {
        // try to start server
        try {
            server = new DBServer(name);
            server.start();
        }
        catch (Exception e) {
            // maybe the server already exists
        }

        queue = new SynchronousQueue<>(true);

        //client = new Client(8192, 65536);
        client = new Client(65536, 65536);
        //client = new Client();
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
                //System.out.println("QUEUE " + object);
                try {
                    queue.put(object);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void close() {

    }

    public Vertex get(String id, VertexType vtype) {
        //System.out.println("REQ get " + id);

        client.sendTCP(new GetRequest(id, vtype));

        try {
            Object obj = queue.take();
            GetResponse reply;
            //System.out.println("RES get " + id);
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
        //System.out.println("REQ put");
        client.sendTCP(new PutRequest(vertex));

        try {
            Object obj = queue.take();
            //System.out.println("RES put");
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
        //System.out.println("REQ update");
        client.sendTCP(new UpdateRequest(vertex));

        try {
            Object obj = queue.take();
            //System.out.println("RES update");
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
        //System.out.println("REQ remove");
        client.sendTCP(new RemoveRequest(vertex));

        try {
            queue.take();
            //System.out.println("RES remove");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void associateEmailToUsername(String email, String username) {
        //System.out.println("REQ associate");
        client.sendTCP(new AssociateEmailToUsernameRequest(email, username));

        try {
            queue.take();
            //System.out.println("RES associate");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String usernameByEmail(String email) {
        //System.out.println("REQ usernameByEmail");
        client.sendTCP(new UsernameByEmailRequest(email));

        try {
            Object obj = queue.take();
            //System.out.println("RES usernameByEmail");
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
        //System.out.println("REQ listByType");
        client.sendTCP(new ListByTypeRequest(vtype));

        try {
            Object obj = queue.take();
            //System.out.println("RES listByType");
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
        //System.out.println("REQ edges");
        client.sendTCP(new EdgesPatternRequest(pattern));

        try {
            Object obj = queue.take();
            //System.out.println("RES edges");
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
        //System.out.println("REQ edges");
        client.sendTCP(new EdgesRequest(center));

        try {
            Object obj = queue.take();
            //System.out.println("RES edges");
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
        //System.out.println("REQ addLinkToGlobal");
        client.sendTCP(new AddLinkToGlobalRequest(globalId, userId));

        try {
            queue.take();
            //System.out.println("RES addLinkToGlobal");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeLinkToGlobal(String globalId, String userId) {
        //System.out.println("REQ removeLinkToGlobal");
        client.sendTCP(new RemoveLinkToGlobalRequest(globalId, userId));

        try {
            queue.take();
            //System.out.println("RES removeLinkToGlobal");
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Set<String> alts(String globalId) {
        //System.out.println("REQ atls");
        client.sendTCP(new AltsRequest(globalId));

        try {
            Object obj = queue.take();
            //System.out.println("RES alts");
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
        DBClient client = new DBClient("dbnode");
        Vertex v = client.get("1/coimbra", VertexType.Entity);
        System.out.println("reply: " + v.raw() + "; " + v.getTs() + "; " + v.getDegree());

        Set<Edge> edges = client.edges(v);
        System.out.println(edges);
    }
}
