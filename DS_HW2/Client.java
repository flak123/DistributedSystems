package distributedsystemshw1;

import java.net.*;
import java.io.*;
import java.util.Scanner;

import java.util.Scanner;

public class Client {
  Socket tcpServer;
  DatagramSocket udpServer;
  Scanner din;
  PrintStream pout;
  
  public void getSocket(String mode, String host, int port) throws IOException {
    if (mode.equals("U")) {
        //UDP
        this.udpServer = new DatagramSocket();
    } else {
        //TCP
        this.tcpServer = new Socket(host, port);
        this.pout = new PrintStream(this.tcpServer.getOutputStream());
        this.din = new Scanner(this.tcpServer.getInputStream());
    }
  }
  
  
  public static void main (String[] args) {
    String hostAddress;
    int tcpPort;
    int udpPort;
    Client myClient = new Client();
    String mode = "T";
    

    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <hostAddress>: the address of the server");
      System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(3) <udpPort>: the port number for UDP connection");
      System.exit(-1);
    }

    hostAddress = args[0];
    tcpPort = Integer.parseInt(args[1]);
    udpPort = Integer.parseInt(args[2]);
    

    Scanner sc = new Scanner(System.in);
    
    while(sc.hasNextLine()) {
        String cmd = sc.nextLine();
        String[] tokens = cmd.split(" ");

        if (tokens[0].equals("setmode")) {
            // TODO: set the mode of communication for sending commands to the server 
            // and display the name of the protocol that will be used in future
            mode = tokens[1];
            int portNum = tcpPort;
            if (mode.equals("U")) {
                portNum = udpPort;
            }
            try{
                myClient.getSocket(mode, hostAddress, portNum);
                System.out.println("Server connection set up using " + mode);
            } catch (Exception e){
                System.out.println("Error: " + e);
            }
        }
        else if (tokens[0].equals("purchase")) {
            // TODO: send appropriate command to the server and display the
            // appropriate responses form the server
            if (mode.equals("U")) {
                try {
                    byte[] buf = new byte[1024];
                    buf = cmd.getBytes();
                    InetAddress address = InetAddress.getByName(hostAddress);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, udpPort);
                    myClient.udpServer.send(packet);
                    packet = new DatagramPacket(buf, buf.length);
                    myClient.udpServer.receive(packet);

                    // display response
                    String received = new String(packet.getData());
                    System.out.println("Response " + received);
                    myClient.udpServer.close();
                } catch (Exception e){
                    System.out.println("Error: " + e);
                }
            } else {
                try {
                    myClient.pout.println(cmd);
                    myClient.pout.flush();
                    //int retValue = din.nextInt();
                    String response = myClient.din.nextLine();
                    System.out.println(response);
                    myClient.tcpServer.close();
                } catch (Exception e){
                    System.out.println("Error: " + e);
                }
            }
        } else if (tokens[0].equals("cancel")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("search")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else if (tokens[0].equals("list")) {
          // TODO: send appropriate command to the server and display the
          // appropriate responses form the server
        } else {
          System.out.println("ERROR: No such command");
        }
    }
  }
}


