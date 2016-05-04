
package chatserver;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

//represent a thread for each client to handle his messages.
class ServerThread extends Thread {

    private ChatServer chatServer;                              //variable to hold ChatServer object
    private Socket socket;                                      //variable to hold the socket

    //constructor
    public ServerThread(ChatServer server, Socket socket) {
        this.socket = socket;                                   //init. socket
        this.chatServer = server;                               //init. chatserver
    }

    //run method of the thread, where we want to do when the thread starts.
    @Override
    public void run() {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream()); //get input stream from the socket
            //inifint loop to read from the client.
            while (true) {
                String msg = dataInputStream.readUTF();         //read the message of the client
                if (msg.startsWith("EXIT")) {                   //check for exit message code.
                    String name = dataInputStream.readUTF();    //get the client's name.
                    chatServer.updateParticipantsList(name);    //update others with the client who left chat
                }
                chatServer.sendToClients(msg);                  //send the message to others.
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
