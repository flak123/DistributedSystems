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
        /*if (args.length != 2) {
            System.out.println("Error: Please run >>java Client HOST PORT");
            System.exit(1);
        }*/
        Client myClient = new Client();
        //hostAddress = args[0];
        //tcpPort = Integer.parseInt(args[1]);
    
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of Servers: ");
        int numServers = Integer.parseInt(sc.nextLine());
        String [] serverInput = new String[numServers];
        for(int i = 0; i < numServers; i++){
            System.out.println("Enter the host:port for Server " + i);
            serverInput[i] = sc.nextLine();
        }
        ServerTable myServers = new ServerTable(numServers, serverInput);
        System.out.println("Enter a command:"); 
        while(sc.hasNextLine()) {
            String cmd = sc.nextLine();
            String[] tokens = cmd.split(" ");
            //int portNum = tcpPort;

            if ((tokens[0].equals("reserve")) || (tokens[0].equals("bookSeat")) || (tokens[0].equals("search")) || (tokens[0].equals("delete"))) {
                try {
                    Boolean connection = false;
                    int i = 0;
                    while(!connection && i < numServers){
                        System.out.println(myServers.serverList[i].hostAddress);
                        System.out.println(myServers.serverList[i].portNum);
                        System.out.println(timeoutTime);
                        try{
                            //myClient.getSocket(myServers.serverList[i].hostAddress, myServers.serverList[i].portNum);
                            // TODO: get this to work. Right now we are double connecting
                            myClient.tcpServer = new Socket();
                            myClient.tcpServer.connect(new InetSocketAddress(myServers.serverList[i].hostAddress, myServers.serverList[i].portNum), timeoutTime);
                            myClient.pout = new PrintStream(myClient.tcpServer.getOutputStream());
                            myClient.din = new Scanner(myClient.tcpServer.getInputStream());
                            connection = true;
                            System.out.println("Connection successful");
                        }catch(SocketTimeoutException e){
                            System.out.println("Connection failed "  + e);
                        }
                        i++;
                    }
                    System.out.println("sending cmd: " + cmd);
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
                    System.out.println("ClientError: " + e);
                }
            } else {
                System.out.println("ERROR: No such command");
            }
        }
    }
}



