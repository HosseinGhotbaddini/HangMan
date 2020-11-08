import Client.Client;
import Client.ClientGUI;
import Server.Server;
import Server.ServerGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MainGUI {
    private static Client client = null;
    private static Server server = null;
    private static JFrame mainFrame;

    public static void main(String[] args) {
        mainFrame = new JFrame("WordGame") {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            }
        };
        mainFrame.setLayout(new GridLayout(1, 2, 3, 3));
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.add(new MainGUI.ClientMaker());
        mainFrame.add(new MainGUI.ServerMaker());
        mainFrame.pack();
        mainFrame.setBounds(80, 50, mainFrame.getWidth() + 12, mainFrame.getHeight() + 2);
        mainFrame.setResizable(false);

        mainFrame.setVisible(true);
    }

    static class ClientMaker extends JPanel {
        JTextField serverAddress = new JTextField("localhost");
        JButton join = new JButton("Join server");

        ClientMaker() {
            setLayout(new GridLayout(4, 1, 0, 3));
            add(new JLabel("Join a server", SwingConstants.CENTER));
            add(new JLabel("Enter server address", SwingConstants.LEFT));
            add(serverAddress);
            add(join);
            serverAddress.addActionListener(new ActionHandler());
            join.addActionListener(new ActionHandler());


        }

        boolean initClient(String userInput) {
            String[] split = userInput.split(":");
            String address = split[0];
            int port = 1973;
            if (split.length > 1)
                try {
                    port = Integer.parseInt(split[1]);
                } catch (Exception e) {
                    System.err.println(split[1] + ": isnt a valid port");
                }
            if (client == null)
                client = new Client();
            try {
                client.initClient(address, port);
            } catch (IOException e) {
                System.out.println("Client init failed.\n" + "UserInput: " + userInput + "\n");
                return false;
            }
            return true;
        }

        class ActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (initClient(serverAddress.getText().toLowerCase().trim())) {
                    new ClientGUI(client);
                    mainFrame.dispose();
                }
            }
        }
    }

    static class ServerMaker extends JPanel {
        JTextField portNumber = new JTextField("1973");
        JButton host = new JButton("Host a server");

        ServerMaker() {
            GridLayout gl = new GridLayout(4, 1);
            gl.setHgap(2);
            setLayout(new GridLayout(4, 1, 0, 3));
            add(new JLabel("Become a server", SwingConstants.CENTER));
            add(new JLabel("Enter port number to host on", SwingConstants.LEFT));
            add(portNumber);
            add(host);
            host.addActionListener(new ActionHandler());
            portNumber.addActionListener(new ActionHandler());
        }

        boolean initServer(String userInput) {
            int portNumber = 1973;
            try {
                portNumber = Integer.parseInt(userInput);
            } catch (Exception e) {
                System.err.println(userInput + ": isn't a valid port");
            }
            try {
                server = new Server(portNumber);
            } catch (Exception e) {
                System.out.println("Server init failed.\n" + "UserInput: " + userInput + "\n");
                return false;
            }
            return true;
        }

        class ActionHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (initServer(portNumber.getText().toLowerCase().trim())) {
                    new ServerGUI(server);
                    mainFrame.dispose();
                }
            }
        }
    }

}
