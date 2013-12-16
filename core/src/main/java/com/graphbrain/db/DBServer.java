package com.graphbrain.db;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.graphbrain.db.messages.*;

import java.io.IOException;

public class DBServer {

    Backend backend;

    public DBServer() {
        backend = new LevelDbBackend();
    }

    public void start() {
        Server server = new Server();
        Network.registerMessages(server.getKryo());
        server.start();
        try {
            server.bind(54555, 54777);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof GetRequest) {
                    GetRequest get = (GetRequest)object;

                    GetResponse response = new GetResponse(backend.get(get.getId(), get.getVtype()));
                    connection.sendTCP(response);
                }
            }
        });
    }
}