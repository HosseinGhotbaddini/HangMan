package Client;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class Client {
    HashMap<Integer, InClientGame> games = new HashMap<Integer, InClientGame>(); // <gameID, game>
    HashMap<Integer, String> playersList = new HashMap<Integer, String>(); // <playerID, playerName>
    PrintWriter sender;
    String playerName = "";
    private Socket playerSocket;
    private HashMap<Integer, Long> lastRequestTime = new HashMap<Integer, Long>();
    private Scanner receiver;
    private Client.DataReceiver dataReceiver; //thread
    private ClientGUI clientGUI;

    public void initClient(String serverID, int portNumber) throws IOException {
        Socket playerSocket = new Socket(serverID, portNumber);
        receiver = new Scanner(playerSocket.getInputStream());
        sender = new PrintWriter(playerSocket.getOutputStream());
        dataReceiver = new Client.DataReceiver();
        dataReceiver.start();

    }

    void setClientGUI(ClientGUI clientGUI) {
        this.clientGUI = clientGUI;
    }

    void requestChangeName(String newName) {
        sender.write("nn§" + newName + "\n");
        sender.flush();
    }

    private void responseChangeName(String newName) {
        if (playerName.equals(newName))
            return;
        playerName = newName;
        clientGUI.update();
    }

    void disconnect() {
        sender.write("dc\n");
        sender.flush();
        try {
            receiver.close();
            sender.close();
            playerSocket.close();
        } catch (Exception e) {
        }
        System.out.println("Disconnecting");
        clientGUI.clientGUI.dispose();
        System.exit(0);
    }

    void newGameRequestFromPlayer(int rivalID) {
        if (lastRequestTime.get(rivalID) + 10000 < System.currentTimeMillis()) {
            sender.write("ngr§" + rivalID + "\n");
            sender.flush();
        }
        lastRequestTime.replace(rivalID, System.currentTimeMillis());
    }

    private void newGameRequestFromRival(int rivalID) {
        clientGUI.newGameRequestFromRival(rivalID);
    }

    void newGameResponseToRival(boolean res, int rivalID) {
        if (!playersList.containsKey(rivalID))
            return;
        if (!res)
            sender.write("dng§" + rivalID + "\n");
        else
            sender.write("sng§" + rivalID + "\n");
        sender.flush();
    }

    private void declineNewGameFromRival(int rivalID) {
        System.out.println(rivalID);
        System.out.println(playersList);
        JOptionPane.showMessageDialog(clientGUI.clientGUI, "[" + playersList.get(rivalID) + "] declined your request.");
        System.out.println(rivalID);
        lastRequestTime.replace(rivalID, System.currentTimeMillis() + 100000);
    }

    private void startNewGame(int rivalID, int gameID) {
        InClientGame newGame = new InClientGame(rivalID, gameID, this);
        games.put(gameID, newGame);
        System.out.println(games);
    }

    private void updateOnlinePlayersList(String[] data) {
        int count = 1;
        HashMap<Integer, String> tmpList = new HashMap<Integer, String>();
        while (count < data.length) {
            tmpList.put(Integer.parseInt(data[count]), data[count + 1]);
            count += 2;
        }

        for (Integer ID : tmpList.keySet())
            if (!playersList.containsKey(ID))
                lastRequestTime.put(ID, 0L);

        playersList = tmpList;
        clientGUI.update();
    }

    private void endGameByRival(int gameID) {
        games.remove(gameID).endGameByRival();
    }

    private class DataReceiver extends Thread {

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            while (true) {
                String[] data = receiver.nextLine().split("§");

                switch (data[0]) {
                    case "nn":
                        responseChangeName(data[1]);
                        break;
                    case "ngr":
                        newGameRequestFromRival(Integer.parseInt(data[1]));
                        break;
                    case "dng":
                        declineNewGameFromRival(Integer.parseInt(data[1]));
                        break;
                    case "ing":
                        startNewGame(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                        break;
                    case "ipl":
                        updateOnlinePlayersList(data);
                        break;
                    case "eg":
                        endGameByRival(Integer.parseInt(data[1]));
                        System.out.println(playerName + " : game ended.");
                        break;
                    case "rt":
                        games.get(Integer.parseInt(data[1])).requestTurn();
                        break;
                    case "sc":
                        games.get(Integer.parseInt(data[1])).setChooser();
                        break;
                    case "ir":
                        games.get(Integer.parseInt(data[1])).informResult(Boolean.parseBoolean(data[2]));
                        break;
                    case "gc":
                        games.get(Integer.parseInt(data[1])).requestGuessChar();
                        break;
                    case "gw":
                        games.get(Integer.parseInt(data[1])).requestGuessWord();
                        break;
                    case "ic":
                        games.get(Integer.parseInt(data[1])).informChars(data[2]);
                        break;
                    case "kick":
                        System.out.println("kicked by server");
                        disconnect();
                        break;

                    default:
                        System.err.println("Client.java receiver failed:\n" + Arrays.toString(data) + "\n");
                }

                if (false)
                    break;
            }
            System.out.println(playerName +  " receiver died in client.");
        }


    }

}
