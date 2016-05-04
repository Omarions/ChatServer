
package chatserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ChatServer extends JFrame {

    JButton startServer;                                        //button to start the server
    JTextField portText;                                        //text box to get the port from the user.
    JLabel portLabel;                                           //label for the text box.
    
    private String portNumber;                                  //variable to hold the text of port.
    private final Hashtable outputStreams;                      //hashtable to maping sockets with streams.
    private List<String> clientsNames;                          //list of connected clients' names
    private ServerSocket serverSocket;                          //server socket

    //constructor
    public ChatServer() {
        outputStreams = new Hashtable();
        clientsNames = new ArrayList<>();
        loadGUI();                                              //load the GUI of the server.
    }

    //load the GUI.
    private void loadGUI() {
        setSize(320, 150);                                      //set the size of the frame.
        setDefaultCloseOperation(EXIT_ON_CLOSE);                //set action of the 'X' button of title bar in the frame.
        setLayout(null);                                        //set the layout to absolute.

        portLabel = new JLabel("Port Number:");                 //create label
        portText = new JTextField("");                          //create text box
        startServer = new JButton("Start Server");              //create button

        portLabel.setSize(150, 30);                             //set size of label
        portLabel.setLocation(20, 20);                          //set the location of label on the frame

        portText.setSize(150, 30);                              //set the size of text box
        portText.setLocation(120, 20);                          //set the location on the frame.

        startServer.setSize(120, 30);                           //set the size of button
        startServer.setLocation(150, 60);                       //set the location on the frame
        startServer.addActionListener(new ActionListener() {    //set the action listener of the button (what do when clicked)
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!portText.getText().isEmpty()) {            //verfy that port is entered
                    portNumber = portText.getText();            //get the text.
                    try {
                        int port = Integer.parseInt(portNumber);    //try to parse the text into integer.
                        startServer.setEnabled(false);
                        portText.setEnabled(false);
                        Thread thread = new Thread(new Runnable() { //create new thread for listening to requests.
                            @Override
                            public void run() {
                                listenToRequests(port);         //listen to requests on port.
                            }
                        });
                        thread.start();                         //start the thread.
                    } catch (NumberFormatException ex) {        //exception if user doesn't enter numbers in the port text box.
                        JOptionPane.showConfirmDialog(null, "Enter numbers only...", "Port Number Error", JOptionPane.ERROR_MESSAGE);
                    }

                } else {
                    JOptionPane.showConfirmDialog(null, "Enter port number from 1000 to 9999", "Port Number Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        //add components to the panel
        add(portLabel);
        add(portText);
        add(startServer);

        setVisible(true);

    }

    //listen to clients connections on port.
    private void listenToRequests(int port) {
        try {
            serverSocket = new ServerSocket(port);                  //create new server socket with the port.
            //inifinte loop to handle all requests.
            while (true) {
                Socket socket = serverSocket.accept();              //accept new connection request.
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream()); //create output stream to use to send to the client.
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());     //create input stream to read from the client.
                outputStreams.put(socket, dataOutputStream);        //add socket with stream to hashtable.

                String clientName = dataInputStream.readUTF();      //get the name of the client from input stream.
                clientsNames.add(clientName);                       //add the name of client to the list of connected clients.

                sendNewNameToClients(clientName);                   //send the client's name to others

                ServerThread serverThread = new ServerThread(this, socket);                         //create new thread for the client.
                serverThread.start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //get the output stream form the hashtable
    private Enumeration getOutputStreams() {
        return outputStreams.elements();
    }

    //send the new client name to the other clients.
    public void sendNewNameToClients(String clientName) {
        String names = "";                                              //variable to rebuld the names of clients from the list.
        for (int i = 0; i < clientsNames.size(); i++) {                     //loop on the list
            names += clientsNames.get(i) + " ";                         //rebuld the names of clients from the list.
        }
        Enumeration e = getOutputStreams();                             //get the the stream Enumerations.
        while (e.hasMoreElements()) {                                   //loop on streams
            try {
                ((DataOutputStream) e.nextElement()).writeUTF("NAMES;" + names);                         //send the names of connected clients.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //remove a client from list and update list of participants
    public void updateParticipantsList(String ClientName) {
        clientsNames.remove(ClientName);                                //remove the client's name from the list.
        String names = "";                                              //variable to hold new names
        for (int i = 0; i < clientsNames.size(); i++) {                     //loop on the list of names
            names += clientsNames.get(i) + " ";                         //rebuild new names.
        }
        Enumeration e = getOutputStreams();                             //get the streams
        while (e.hasMoreElements()) {                                   //loop on streams.
            try {
                ((DataOutputStream) e.nextElement()).writeUTF("NAMES;" + names);    //send new names to clients.
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Used to send message to all clients
    public void sendToClients(String msg) {
        Enumeration e = getOutputStreams();                             //get the streams from hashtable
        while (e.hasMoreElements()) {
            try {
                if (msg.startsWith("EXIT")) {                           //check for exit message code.
                    ((DataOutputStream) e.nextElement()).writeUTF(msg); //send message
                } else {                                                 //for normal message (NOT EXIST)
                    ((DataOutputStream) e.nextElement()).writeUTF("MESSAGE;" + msg);    //send the message.
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //main method of application
    public static void main(String[] args) {
        new ChatServer();                              //create the frame and show it..
    }
}
