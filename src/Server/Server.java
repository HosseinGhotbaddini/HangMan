package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private static final int serverSizeLimit = 10;
    ServerSocket getter;
    HashMap<Integer, InServerClient> players = new HashMap<Integer, InServerClient>();
    ServerGUI serverGUI;

    public Server(int portNumber) throws IOException {
        getter = new ServerSocket(portNumber);
        new Server.Acceptor(this).start();
    }

    void setServerGUI(ServerGUI serverGUI) {
        this.serverGUI = serverGUI;
    }

    void informPlayersList() {
        for (InServerClient player : players.values())
            player.informPlayersList();
        serverGUI.update();
    }

    private class Acceptor extends Thread {
        Server server;

        private Acceptor(Server server) {
            this.server = server;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            while (true) {
                try {
                    Socket inServerPlayerSocket = getter.accept();

                    if (players.size() < serverSizeLimit) {
                        InServerClient tmpPlayer = new InServerClient(inServerPlayerSocket, server);
                        players.put(tmpPlayer.playerID, tmpPlayer);
                        informPlayersList();
                    } else
                        inServerPlayerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
