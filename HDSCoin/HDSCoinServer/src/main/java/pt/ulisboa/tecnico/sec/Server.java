package pt.ulisboa.tecnico.sec;

import javax.xml.ws.Endpoint;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class Server {
    public static ServerSocket mainSocket;



    public static void main(String[] args){
        try{
            mainSocket = new ServerSocket(1381);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

        while(true){
            try {
                Socket client = mainSocket.accept();
                DataInputStream in = new DataInputStream(client.getInputStream());
                byte[] received = new byte[1024];
                int count = 0;
                System.out.println("Message: " + in.readUTF());

            }catch(IOException ioe){
                ioe.printStackTrace();
            }
        }

    }

}
