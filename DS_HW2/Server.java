
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.*;
import java.io.*;
import java.util.stream.Stream;
import java.util.ArrayList;

public class Server {
  InventoryTable inventory;
  OrderTable orders;
  
  public Server(String fileName){
      // parse the inventory file
    this.inventory = new InventoryTable(fileName);
    this.orders = new OrderTable();
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
                  Thread t = new ServerTcpThread(this.ns.inventory, this.ns.orders, s);
                  t.start();
              }
          } catch (Exception e) {
            System.err.println("Server aborted:" + e);
          }
      }
  }
  public static class udpSocket implements Runnable {
      int portNum;
      Server ns;
      
      public udpSocket(int portNum, Server ns) {
          this.portNum = portNum;
          this.ns = ns;
      } 
      public void run() {
          try {
              DatagramSocket dataSocket = new DatagramSocket(this.portNum);
              while(true) {              
                  Thread t = new ServerUdpThread(this.ns.inventory, this.ns.orders, dataSocket);
                  t.start();
                  t.join();  
              }
          } catch (Exception e) {
            System.err.println("Server aborted:" + e);
          }
      }
  }
  
  
  public static void main (String[] args) {
    int tcpPort;
    int udpPort;
    if (args.length != 3) {
      System.out.println("ERROR: Provide 3 arguments");
      System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
      System.out.println("\t(2) <udpPort>: the port number for UDP connection");
      System.out.println("\t(3) <file>: the file of inventory");

      System.exit(-1);
    }
    tcpPort = Integer.parseInt(args[0]);
    udpPort = Integer.parseInt(args[1]);
    String fileName = args[2];
    

    // TODO: handle request from clients
    //Listener
    Server ns = new Server(fileName);
    System.out.println("Server started:");
    tcpSocket s1 = new tcpSocket(tcpPort, ns);
    udpSocket s2 = new udpSocket(udpPort, ns);
    Thread t1=new Thread(s1);
    Thread t2=new Thread(s2);
    t1.start();
    t2.start();
    
    
    
    
  }
}
