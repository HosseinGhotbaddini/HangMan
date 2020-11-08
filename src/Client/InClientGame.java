package Client;

import javax.swing.*;

class InClientGame {
    int gameID;
    int rivalID;
    String shownChars = "", chosenWord = "";
    byte myScore = 0, rivalScore = 0;
    Client client;
    boolean isChooser = false;
    private GameGUI gameGUI;

    InClientGame(int rivalID, int gameID, Client client) {
        this.gameID = gameID;
        this.rivalID = rivalID;
        this.client = client;
        gameGUI = new GameGUI(this);
    }

    void requestTurn() {
        gameGUI.requestTurn();
    }

    void responseTurn(int turn) {
        client.sender.write("st§" + gameID + "§" + turn + "\n");
        client.sender.flush();
        System.out.println(client.playerName + " : " + gameID + "  t: " + turn);
    }

    void setChooser() {
        isChooser = true;
        gameGUI.requestWord();
    }

    void setWord(String word) {
        chosenWord = word;
        client.sender.write("sw§" + gameID + "§" + word + "\n");
        client.sender.flush();
    }

    void informResult(boolean result) {
        if (result) {
            if (isChooser) {
                ++rivalScore;
            } else {
                ++myScore;
            }
        }
        isChooser = false;
        chosenWord = shownChars = "";
        gameGUI.update();
    }

    void requestGuessChar() {
        gameGUI.guessChar();
    }

    void guessChar(char triedChar) {
        client.sender.write("gc§" + gameID + "§" + triedChar + "\n");
        client.sender.flush();
    }

    void requestGuessWord() {
        gameGUI.guessWord();
    }

    void guessWord(String triedWord) {
        client.sender.write("gw§" + gameID + "§" + triedWord + "\n");
        client.sender.flush();
    }

    void informChars(String shownChars) {
        this.shownChars = shownChars;
        gameGUI.update();
    }

    void endGameByPlayer() {
        client.sender.write("eg§" + gameID + "\n");
        client.sender.flush();
        JOptionPane.showMessageDialog(gameGUI.gameGUI, "End: You " + myScore + " : " + rivalScore + " " + client.playersList.get(rivalID));
        gameGUI.gameGUI.dispose();
        gameGUI = null;
        client.games.remove(gameID);
    }

    void endGameByRival() {
        JOptionPane.showMessageDialog(gameGUI.gameGUI, "End: You " + myScore + " : " + rivalScore + " " + client.playersList.get(rivalID));
        gameGUI.gameGUI.dispose();
        gameGUI = null;
        client.games.remove(gameID);
    }

}
