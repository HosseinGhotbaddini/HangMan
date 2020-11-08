package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class ServerGUI {
    private JFrame serverGUI;
    private Server server;
    private JList playersList;
    private JLabel portLabel;
    private JButton stopServerButton;
    private JButton kickButton;
    private JPanel rightSide;
    private HashMap<String, Integer> names;

    public ServerGUI(Server server) {
        this.server = server;
        server.setServerGUI(this);
        serverGUI = new JFrame("Server GUI");
        playersList = new JList();
        portLabel = new JLabel("Server running on port: " + server.getter.getLocalPort(), SwingConstants.CENTER);
        stopServerButton = new JButton("Stop server");
        kickButton = new JButton("Kick player");
        rightSide = new JPanel(new GridLayout(3, 1, 0, 3));

        kickButton.addActionListener(e -> {
            String playerName = playersList.getSelectedValue().toString();
            if (playerName == null)
                return;
            if (JOptionPane.showConfirmDialog(serverGUI, "Kick [" + playerName + "]", "Kick", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                System.out.println("Kicking [" + playerName + "]");
                server.players.get(names.get(playerName)).kick();
            }
        });

        stopServerButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(serverGUI, "Are you sure?", "Stop server", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                for (InServerClient player : server.players.values()) {
                    player.kick();
                }
                System.exit(0);
            }
        });

        serverGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (InServerClient player : server.players.values()) {
                    player.kick();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });


        rightSide.add(portLabel);
        rightSide.add(kickButton);
        rightSide.add(stopServerButton);

        serverGUI.setLayout(new GridLayout(1, 2, 3, 3));
        serverGUI.add(playersList);
        serverGUI.add(rightSide);

        serverGUI.setBounds(100, 100, 400, 200);
        serverGUI.setResizable(false);
        serverGUI.setVisible(true);
        serverGUI.repaint();
    }

    void update() {
        names = new HashMap<String, Integer>();
        for (InServerClient player : server.players.values())
            names.put(player.playerName, player.playerID);
        playersList.setListData(names.keySet().toArray());
        serverGUI.repaint();
    }

}
