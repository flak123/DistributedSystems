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
        String hostAddress;
        int tcpPort;
        int timeoutTime = 100;
        Client myClient = new Client();
        hostAddress = args[0];
        tcpPort = Integer.parseInt(args[1]);
    
        Scanner sc = new Scanner(System.in);
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
            int portNum = tcpPort;

            if ((tokens[0].equals("reserve")) || (tokens[0].equals("bookSeat")) || (tokens[0].equals("search")) || (tokens[0].equals("delete"))) {
                // TODO: send appropriate command to the server and display the
                // appropriate responses form the server
                try {
                    Boolean connection = false;
                    while(!connection){
                        for(int i = 0; i < numServers; i++){
                            myClient.getSocket(myServers.serverList[i].hostAddress, myServers.serverList[i].portNum);
                            try{
                                myClient.tcpServer.connect(new InetSocketAddress(myServers.serverList[i].hostAddress, myServers.serverList[i].portNum), timeoutTime);
                                connection = true;
                            }catch(SocketTimeoutException e){
                                System.out.println("Connection failed "  + e);
                            }
                        }
                    }
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


