package DS_HW2;


import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    int myId;
    String myHost;
    int myPort;
    LamportClock lcv;
    LamportMutex lamportMutex;
    
    public class ServerInfo {
        int servId;
        String hostName;
        int portNum;

        public ServerInfo(int id, String hostName, int portNum) {
            this.servId = id;
            this.hostName = hostName;
            this.portNum = portNum;
        }
    }

    SeatingTable seats;
    ArrayList<ServerInfo> serverDirectory = new ArrayList<ServerInfo>();
  
    public Server(int id, int numServ){
        this.myId = id;
        this.lamportMutex = new LamportMutex(id);
        Scanner scanner = new Scanner(System.in);
        String userInput;
        String[] userInputPieces;
        String otherHost;
        int otherPort;


        for (int i = 0; i < numServ; i++) {
            System.out.println("Enter the host:port for Server " + i);
            userInput = scanner.nextLine();
            // XXX: assumes perfect input. Should add err checking
            userInputPieces = userInput.split(":");
            otherHost = userInputPieces[0];
            otherPort = Integer.parseInt(userInputPieces[1]);
            ServerInfo s = new ServerInfo(i, otherHost, otherPort);
            this.serverDirectory.add(s);    
        }
        this.myHost = this.serverDirectory.get(this.myId).hostName;
        this.myPort = this.serverDirectory.get(this.myId).portNum;
        // TODO: how to initialiize
        //lamportMutex.requestCS();
        // TODO: now in CS. HERE we need to copy ServerTable
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
                    // Server sync requests are:
                    // request <pid> <lcv>
                    // ack <pid> <lcv>
                    // release <pid> <lcv>
                    Scanner sc = new Scanner(s.getInputStream());
                    String command;
                    command = sc.nextLine();
                    String[] tokens = command.replaceAll("(\\r|\\n)", "").split(" ");
                    if (tokens[0] == "request" || 
                        tokens[0] == "release" || 
                        tokens[0] == "ack"
                    ) {
                        // Server request. Do sync stuff.
                        ns.lamportMutex.handleMsg(command);
                    } else {
                        // spawn ServerThread to handle client requests
                        ns.lamportMutex.requestCS(s);
                        Thread t = new ServerThread(this.ns.seats, s);
                        t.start();
                        t.join();
                        ns.lamportMutex.releaseCS();
                    }
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

        //Listener
        Server ns = new Server(serverID, numServ);
        System.out.println("Server started:");
        tcpSocket s1 = new tcpSocket(ns.myPort, ns);
        Thread t1=new Thread(s1);
        t1.start();   
    
    }
}
