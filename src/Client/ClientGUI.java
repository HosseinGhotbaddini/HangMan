package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGUI {
    JFrame clientGUI;
    private Client client;
    private JList playersList;
    private JLabel nameLabel;
    private JButton renameButton;
    private JButton disconnectButton;
    private JButton startGameButton;
    private JPanel rightSide;


    public ClientGUI(Client client) {
        this.client = client;
        client.setClientGUI(this);
        clientGUI = new JFrame("Client GUI");
        playersList = new JList();
        renameButton = new JButton("Rename");
        disconnectButton = new JButton("Disconnect");
        startGameButton = new JButton("Start Game");
        nameLabel = new JLabel(client.playerName, SwingConstants.CENTER);
        rightSide = new JPanel(new GridLayout(4, 1, 0, 3));

        renameButton.addActionListener(e -> {
            String newName = JOptionPane.showInputDialog(clientGUI, "Enter new name");
            while (newName.contains("§")) {
                newName = JOptionPane.showInputDialog("Enter new name (can't contain '§'s)");
            }
            client.requestChangeName(newName);
        });

        disconnectButton.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(clientGUI, "Are you sure?", "Disconnect", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                client.disconnect();
        });

        startGameButton.addActionListener(e -> {
            int rivalID = -1;
            String rivalName = playersList.getSelectedValue().toString();
            if (rivalName == null)
                return;
            for (int playerID : client.playersList.keySet())
                if (client.playersList.get(playerID).equals(rivalName)) {
                    rivalID = playerID;
                    break;
                }
            if (rivalID == -1)
                return;
            client.newGameRequestFromPlayer(rivalID);
        });

        clientGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnect();
            }
        });

        rightSide.add(nameLabel);
        rightSide.add(renameButton);
        rightSide.add(startGameButton);
        rightSide.add(disconnectButton);

        clientGUI.setLayout(new GridLayout(1, 2, 3, 3));
        clientGUI.add(playersList);
        clientGUI.add(rightSide);

        clientGUI.setBounds(100, 100, 380, 190);
        clientGUI.setResizable(false);
        clientGUI.setVisible(true);
        clientGUI.repaint();
    }

    void update() {
        playersList.setListData(client.playersList.values().toArray());
        nameLabel.setText(client.playerName);
        clientGUI.repaint();
    }

    void newGameRequestFromRival(int rivalID) {
        String rivalName = client.playersList.get(rivalID);
        if (JOptionPane.showConfirmDialog(clientGUI, "Do you want to play with [" + rivalName + "] ?", "New game request", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            client.newGameResponseToRival(true, rivalID);
            System.out.println("ngr accepted: " + rivalName);
        } else {
            client.newGameResponseToRival(false, rivalID);
            System.out.println("ngr refused: " + rivalName);
        }
    }

}
