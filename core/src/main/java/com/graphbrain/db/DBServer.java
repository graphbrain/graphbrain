package com.graphbrain.db;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.graphbrain.db.messages.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class DBServer {

    Backend backend;

    public DBServer(String name) {
        backend = new LevelDbBackend(name);
    }

    public void start() {
        Log.set(Log.LEVEL_DEBUG);

        Server server = new Server(65536, 65536);
        //Server server = new Server();
        Network.registerMessages(server.getKryo());
        server.start();

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                try {
                if (object instanceof GetRequest) {
                    GetRequest get = (GetRequest)object;

                    GetResponse response = new GetResponse(backend.get(get.getId(), get.getVtype()));
                    connection.sendTCP(response);
                }
                else if (object instanceof PutRequest) {
                    PutRequest put = (PutRequest)object;

                    backend.put(put.getVertex());

                    connection.sendTCP(new OK());
                }
                else if (object instanceof UpdateRequest) {
                    UpdateRequest update = (UpdateRequest)object;

                    backend.update(update.getVertex());

                    connection.sendTCP(new OK());
                }
                else if (object instanceof RemoveRequest) {
                    RemoveRequest remove = (RemoveRequest)object;

                    backend.remove(remove.getVertex());

                    connection.sendTCP(new OK());
                }
                else if (object instanceof AssociateEmailToUsernameRequest) {
                    AssociateEmailToUsernameRequest req = (AssociateEmailToUsernameRequest)object;

                    backend.associateEmailToUsername(req.getEmail(), req.getUsername());

                    connection.sendTCP(new OK());
                }
                else if (object instanceof UsernameByEmailRequest) {
                    UsernameByEmailRequest req = (UsernameByEmailRequest)object;

                    String username = backend.usernameByEmail(req.getEmail());

                    connection.sendTCP(new UsernameByEmailResponse(username));
                }
                else if (object instanceof ListByTypeRequest) {
                    ListByTypeRequest req = (ListByTypeRequest)object;

                    List<Vertex> vertices = backend.listByType(req.getVtype());

                    connection.sendTCP(new ListByTypeResponse(vertices));
                }
                else if (object instanceof EdgesRequest) {
                    EdgesRequest req = (EdgesRequest)object;

                    Set<Edge> edges = backend.edges(req.getVertex());

                    connection.sendTCP(new EdgesResponse(edges));
                }
                else if (object instanceof EdgesPatternRequest) {
                    EdgesPatternRequest req = (EdgesPatternRequest)object;

                    Set<Edge> edges = backend.edges(req.getEdge());

                    connection.sendTCP(new EdgesResponse(edges));
                }
                else if (object instanceof AddLinkToGlobalRequest) {
                    AddLinkToGlobalRequest req = (AddLinkToGlobalRequest)object;

                    backend.addLinkToGlobal(req.getGlobalId(), req.getUserId());

                    connection.sendTCP(new OK());
                }
                else if (object instanceof RemoveLinkToGlobalRequest) {
                    RemoveLinkToGlobalRequest req = (RemoveLinkToGlobalRequest)object;

                    backend.removeLinkToGlobal(req.getGlobalId(), req.getUserId());

                    connection.sendTCP(new OK());
                }
                else if (object instanceof AltsRequest) {
                    AltsRequest req = (AltsRequest)object;

                    Set<String> alts = backend.alts(req.getGlobalId());

                    connection.sendTCP(new AltsResponse(alts));
                }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            server.bind(54555, 54777);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        DBServer server;

        try {
            server = new DBServer("dbnode");
            server.start();
        }
        catch (Exception e) {
            // maybe the server already exists
            e.printStackTrace();
        }
    }
}