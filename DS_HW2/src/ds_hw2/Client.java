package DS_HW2;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
  Socket tcpServer;
  Scanner din;
  PrintStream pout;
  
    public void getSocket(String host, int port) throws IOException {
        this.tcpServer = new Socket(host, port);
        this.pout = new PrintStream(this.tcpServer.getOutputStream());
        this.din = new Scanner(this.tcpServer.getInputStream());
    }
  
  
    public static void main (String[] args) {

        Client myClient = new Client();

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }
    
        Scanner sc = new Scanner(System.in);
        System.out.println("How many servers are there?");
        int numServers = Integer.parseInt(sc.nextLine());
        String [] serverInput = new String[numServers];
        for(int i = 0; i < numServers; i++){
            serverInput[i] = sc.nextLine();
        }
        ServerTable myServers = new ServerTable(numServers, serverInput);
        System.out.println("Enter a command:"); 
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");
            

            if ((tokens[0].equals("purchase")) || (tokens[0].equals("cancel")) || (tokens[0].equals("search")) || (tokens[0].equals("list"))) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
                try {
                    myClient.getSocket(hostAddress, portNum);
                    myClient.pout.println(cmd);
                    myClient.pout.flush();
                    //int retValue = din.nextInt();
                    String response = myClient.din.nextLine();
                    System.out.println(response);
                    while(myClient.din.hasNextLine()){
                        response = myClient.din.nextLine();
                        System.out.println(response);
                    }
                    myClient.tcpServer.close();
                } catch (Exception e){
                    System.out.println("Error: " + e);
                }
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }
}


