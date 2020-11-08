package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

class InServerClient {
    static HashMap<Integer, InServerGame> games = new HashMap<Integer, InServerGame>();
    private static int nameNum = 0;
    private static int gameNum = 0;
    PrintWriter sender;
    String playerName = "NoName ";
    Integer playerID;
    private InServerClient.DataReceiver dataReceiver; //thread
    private Socket inServerPlayerSocket;
    private Scanner receiver;
    private Server server;

    InServerClient(Socket inServerPlayerSocket, Server server) throws IOException {
        this.server = server;
        this.inServerPlayerSocket = inServerPlayerSocket;
        receiver = new Scanner(inServerPlayerSocket.getInputStream());
        sender = new PrintWriter(inServerPlayerSocket.getOutputStream());
        dataReceiver = new InServerClient.DataReceiver();
        dataReceiver.start();
        playerName += (playerID = ++nameNum);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sender.write("nn§" + this.playerName + "\n");
        sender.flush();
        server.serverGUI.update();
    }

    void informPlayersList() {
        StringBuilder res = new StringBuilder("ipl");
        for (InServerClient player : server.players.values())
            if (player != this) {
                res.append("§");
                res.append(player.playerID);
                res.append("§");
                res.append(player.playerName);

            }
        res.append("\n");
        sender.write(res.toString());
        sender.flush();
    }

    private void declineNewGame(int rivalID) {
        sender.write("dng§" + rivalID + "\n");
        sender.flush();
    }

    private void changeName(String playerName) {
        boolean nameChangePossible = true;
        for (InServerClient player : server.players.values()) {
            if (player.playerName.equals(playerName)) {
                nameChangePossible = false;
                break;
            }
        }
        if (nameChangePossible)
            this.playerName = playerName;
        sender.write("nn§" + this.playerName + "\n");
        sender.flush();
        server.serverGUI.update();
        server.informPlayersList();
    }

    private void disconnect() {
        for (InServerGame game : games.values()) {
            InServerClient rival;
            if (game.player[0] == this)
                rival = game.player[1];
            else
                rival = game.player[0];
            game.endGame(rival);
        }

        receiver.close();
        sender.close();
        try {
            inServerPlayerSocket.close();
        } catch (Exception e) {
        }
        server.players.remove(this.playerID);
        server.informPlayersList();
        server.serverGUI.update();
        System.gc();
        dataReceiver.stop();
    }

    private void newGameRequestFromMe(Integer rivalID) {
        InServerClient rival = server.players.get(rivalID);
        if (rival == null)
            return;

        if (!isPlayingWith(rival)) {
            rival.newGameRequestFromRival(this);
        }
    }

    private boolean isPlayingWith(InServerClient rival) {
        for (InServerGame game : games.values())
            for (int i = 0; i < 2; ++i)
                if (game.player[i] == rival)
                    return true;
        return false;
    }

    private void newGameRequestFromRival(InServerClient rival) {
        sender.write("ngr§" + rival.playerID + "\n");
        sender.flush();
    }

    private void startNewGame(Integer rivalID) {
        InServerClient rival = server.players.get(rivalID);
        if (rival == null)
            return;
        new InServerGame(this, rival, ++gameNum);
        System.out.println("startNewGame\n" + games);
    }

    void informChars(String shownChars, int gameID) {
        sender.write("ic§" + gameID + "§" + shownChars + "\n");
        sender.flush();

    }

    void setChooser(int gameID) {
        sender.write("sc§" + gameID + "\n");
        sender.flush();
    }

    void requestTurn(int gameID) {
        sender.write("rt§" + gameID + "\n");
        sender.flush();
    }

    void guessWord(int gameID) {
        sender.write("gw§" + gameID + "\n");
        sender.flush();
    }

    void informResult(boolean res, int gameID) {
        sender.write("ir§" + gameID + "§" + res + "\n");
        sender.flush();
    }

    void guessChar(int gameID) {
        sender.write("gc§" + gameID + "\n");
        sender.flush();
    }

    private void endGame(int gameID) {
        InServerGame game = games.get(gameID);
        games.remove(gameID);
        InServerClient rival;
        if (game.player[0] == this)
            rival = game.player[1];
        else
            rival = game.player[0];
        game.endGame(rival);
    }

    void kick() {
        sender.write("kick\n");
        sender.flush();
    }

    private class DataReceiver extends Thread {
        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            while (true) {
                try {
                    String[] data = receiver.nextLine().split("§");
                    System.out.println(Arrays.toString(data));
                    switch (data[0]) {
                        case "nn":
                            changeName(data[1]);
                            break;
                        case "dc":
                            disconnect();
                            break;
                        case "ngr":
                            newGameRequestFromMe(Integer.parseInt(data[1]));
                            break;
                        case "dng":
                            server.players.get(Integer.parseInt(data[1])).declineNewGame(playerID);
                            break;
                        case "sng":
                            startNewGame(Integer.parseInt(data[1]));
                            break;
                        case "st":
                            InServerGame game = games.get(Integer.parseInt(data[1]));
                            System.out.println(game);
                            System.out.println(Arrays.toString(games.values().toArray()));
                            game.setTurnByPlayer(Byte.parseByte(data[2]));
                            break;
                        case "sw":
                            games.get(Integer.parseInt(data[1])).setWord(data[2]);
                            break;
                        case "gc":
                            games.get(Integer.parseInt(data[1])).guessChar(data[2].charAt(0));
                            break;
                        case "gw":
                            games.get(Integer.parseInt(data[1])).guessWord(data[2]);
                            break;
                        case "eg":
                            endGame(Integer.parseInt(data[1]));
                            break;

                        default:
                            System.out.println("InServerClient.java receiver failed:\n" + Arrays.toString(data) + "\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Player [" + playerID + "] receiver failed");
                } finally {
                    if (false)
                        break;
                }
            }
            System.out.println(playerName + "receiver diedin server.");
        }
    }
}
