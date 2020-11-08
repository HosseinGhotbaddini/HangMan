package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class GameGUI {
    JFrame gameGUI;
    private JLabel shownCharsLabel;
    private JLabel namesScoresLabel;
    private JButton endGameButton;
    private InClientGame game;

    GameGUI(InClientGame game) {
        this.game = game;
        gameGUI = new JFrame("Game [" + game.gameID + "] + " + game.client.playerName);
        shownCharsLabel = new JLabel("", SwingConstants.CENTER);
        namesScoresLabel = new JLabel(" You 0  : 0 Rival ", SwingConstants.CENTER);
        endGameButton = new JButton("End game");

        endGameButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(gameGUI, "Are you sure?", "End game", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                game.endGameByPlayer();
        });

        gameGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                game.endGameByPlayer();
            }
        });

        gameGUI.setLayout(new GridLayout(3, 1, 0, 3));
        gameGUI.add(namesScoresLabel);
        gameGUI.add(shownCharsLabel);
        gameGUI.add(endGameButton);

        gameGUI.setBounds(500, 80, 300, 240);
        gameGUI.setResizable(false);
        gameGUI.setVisible(true);
        gameGUI.repaint();

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1200);
                        update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    void update() {
        String word = game.isChooser ? game.chosenWord : game.shownChars;
        namesScoresLabel.setText(" You " + game.myScore + " : " + game.rivalScore + " " + game.client.playersList.get(game.rivalID) + " ");
        StringBuilder shownChars = new StringBuilder("");
        if (word.length() > 0) {
            for (char c : word.toCharArray())
                shownChars.append(c != (char) 0 ? (c + " ") : "_ ");
            shownChars.deleteCharAt(Math.max(0, shownChars.length() - 1));
        }
        shownCharsLabel.setText(shownChars.toString());
        gameGUI.repaint();
    }

    void requestTurn() {
        int turn = 5;
        while (true) {
            try {
                turn = Integer.parseInt(JOptionPane.showInputDialog(gameGUI, "How many turns?",
                        "5", JOptionPane.QUESTION_MESSAGE, null, new String[]{"5", "10", "15", "20"},
                        "5").toString());
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            if (turn > 0 && turn < 30)
                break;
        }
        game.responseTurn(turn);
    }

    void requestWord() {
        String res = "wordword";
        while (true) {
            res = JOptionPane.showInputDialog(gameGUI, "Choose a word");
            if (!res.contains(" ") && !res.contains("§") && !res.contains("!"))
                break;
        }
        game.setWord(res.toLowerCase());
    }

    void guessChar() {
        char res = 'c';
        try {
            res = JOptionPane.showInputDialog(null, "Try a charachter").charAt(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        game.guessChar(res);
    }

    void guessWord() {
        String res = "wordword";
        while (true) {
            res = JOptionPane.showInputDialog(null, "Try a word");
            if (res.length() > 0)
                break;
        }
        game.guessWord(res.toLowerCase());
    }


}
