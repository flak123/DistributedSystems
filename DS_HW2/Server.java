
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.*;
import java.io.*;
import java.util.stream.Stream;
import java.util.ArrayList;

public class Server {
    
    public class ServerInfo {
        int myId;
        String hostName;
        int portNum;

        public ServerInfo(int id, String hostName, int portNum) {
            this.id = id;
            this.hostName = hostName;
            this.portNum = portNum;
        }
    }

    SeatingTable seats;
    ArrayList<ServerInfo> serverDirectory = new ArrayList<ServerInfo>();
  
    public Server(){
        // TODO: sync stuff here
    }
  
    public static class tcpSocket implements Runnable {
        int portNum;
        Server ns;
      
        public tcpSocket(int portNum, Server ns) {
            this.portNum = portNum;
            this.ns = ns;
        } 
        public void run() {
            try {
                ServerSocket listener = new ServerSocket(this.portNum);
                Socket s;
                while((s = listener.accept()) != null) {
                    Thread t = new ServerTcpThread(this.ns.seats, s);
                    t.start();
                }
            } catch (Exception e) {
              System.err.println("Server aborted:" + e);
            }
        }
    }
  
    public static void main (String[] args) {
        int serverID;
        int numServ;
        int numSeats;
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <serverID>: the unique ID of this server");
            System.out.println("\t(2) <numServ>: the number of servers");
            System.out.println("\t(3) <numSeats>: the number of seats in the theatre");

            System.exit(-1);
        }
        serverID = Integer.parseInt(args[0]);
        numServ = Integer.parseInt(args[1]);
        numSeats = Integer.parseInt(args[2]);
        
        Scanner scanner = new Scanner(System.in);
        String userInput;
        String[] userInputPieces;
        String otherHost;
        int otherPort;
        ServerInfo s;
        for (int i = 0; i < numServ; i++) {
            System.out.printlin("Enter the host:port for Server " + i);
            userInput = scanner.nextLine();
            // XXX: assumes perfect input. Should add err checking
            userInputPieces = userInput.split(":");
            otherHost = userInputPieces[0];
            otherPort = Integer.parseInt(userPieces[1]);
            s = ServerInfo(i, otherHost, otherPort);
            serverDirectory.add(s);    
        }   

        //Listener
        Server ns = new Server();
        System.out.println("Server started:");
        tcpSocket s1 = new tcpSocket(serverDirectory[myId].portNum, ns);
        Thread t1=new Thread(s1);
        t1.start();   
    
    }
}
