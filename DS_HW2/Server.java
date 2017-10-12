
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {
    int myId;
    String myHost;
    int myPort;
    LamportClock lcv;
    LamportMutex lamportMutex;
    SeatingTable seats;
    ServerTable serverDirectory;
    int numSeats;
  
    public Server(int id, int numServ, int numSeats){
        this.myId = id-1;
        this.numSeats = numSeats;
        this.seats = new SeatingTable(this.numSeats);
        Scanner scanner = new Scanner(System.in);
        String userInput;
        String[] userInputPieces;
        String otherHost;
        int otherPort;

        String[] servers = new String[numServ];
        for (int i = 0; i < numServ; i++) {
            System.out.println("Enter the host:port for Server " + i);
            userInput = scanner.nextLine();
            // XXX: assumes perfect input. Should add err checking
            servers[i] = userInput;    
        }
        this.serverDirectory = new ServerTable(numServ, servers);
        this.lamportMutex = new LamportMutex(this.myId, this.serverDirectory, this.seats);
        this.myHost = this.serverDirectory.serverList[this.myId].hostAddress;
        this.myPort = this.serverDirectory.serverList[this.myId].portNum;
        // initialiize
        try {
            Socket s = new Socket();
            this.lamportMutex.requestCS(s);
            this.seats = this.lamportMutex.getInitial(s);
            this.lamportMutex.releaseCS();
        } catch (Exception e) {
            System.err.println("ServerError:" + e);
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
        Server ns = new Server(serverID, numServ, numSeats);
        System.out.println("Server started:");
        try {
            ServerSocket listener = new ServerSocket(ns.myPort);
            Socket s;
            while((s = listener.accept()) != null) {
                // Server sync requests are:
                // request <pid> <lcv>
                // ack <pid> <lcv>
                // release <pid> <lcv>
                Scanner sc = new Scanner(s.getInputStream());
                String command;
                command = sc.nextLine();
                System.out.println("Recieved: " + command);
                String[] tokens = command.replaceAll("(\\r|\\n)", "").split(" ");
                if (tokens[0].equals("request") || 
                    tokens[0].equals("release") || 
                    tokens[0].equals("ack")     ||
                    tokens[0].equals("init")    ||
                    tokens[0].equals("initResp")
                ) {
                    ns.lamportMutex.handleMsg(command);
                } else {
                    // spawn ServerThread to handle client requests
                    System.out.println("Waiting on cs...");
                    ns.lamportMutex.requestCS(s);
                    System.out.println("Inside CS!!!");
                    Thread t = new ServerThread(ns.seats, s, command);
                    t.start();
                    t.join();
                    // TODO: send seating table to all other servers
                    ns.lamportMutex.sendAllSeats();
                    ns.lamportMutex.releaseCS();
                    System.out.println("CS RELEASED");
                    // Status of system
                    for(int seat=0; seat < ns.seats.seatArray.length; seat++) {
                        System.out.println(seat + ":  "+  ns.seats.seatArray[seat].name);
                    }
                }
            }
        } catch (Exception e) {
          System.err.println("Server aborted:" + e);
        }
    }
}
