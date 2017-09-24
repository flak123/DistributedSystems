package distributedsystemshw1;

import java.net.*; import java.io.*; import java.util.*;
public class ServerThread extends Thread {
    InventoryTable inventory;
    OrderTable orders;
    Socket theTcpClient;
    DatagramSocket theUdpClient;
    //DatagramPacket datapacket, returnpacket;
    int len = 1024;

    public ServerThread(InventoryTable inventory, OrderTable orderTable, Socket s1, DatagramSocket s2) {
        System.out.println("Thread constructorrrrr");
        this.inventory = inventory;
        this.orders = orderTable;
        this.theTcpClient = s1;
        this.theUdpClient = s2;
        //this.socket = new DatagramSocket(udpPort);
        //this.in = new BufferedReader();
    }
    public void run() {
        System.out.println("Thread running");
        String command;
        
        try {
            //UPDP
            
            byte[] buf = new byte[len];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.theUdpClient.receive(packet);
            command = new String(packet.getData());
            
            
            //TCP
            /*
            Scanner sc = new Scanner(theTcpClient.getInputStream());
            PrintWriter pout = new PrintWriter(theTcpClient.getOutputStream());
            command = sc.nextLine();
            */
            
            System.out.println("received:" + command);
            String[] tokens = command.split(" ");
            
            //TCP
            //Scanner st = new Scanner(command);          
            //String tag = st.next();
            
            
            //UDP
            String packetString = "No response";
            
            //common
            if (tokens[0].equals("purchase")) {
                Integer successCode = inventory.update(tokens[2],Integer.parseInt(tokens[3]));
                if (successCode == 0) {
                    OrderTable.Order newOrder = orders.add(tokens[1], tokens[2], Integer.parseInt(tokens[3]));
                    packetString = "Your order has been placed, " + 
                        newOrder.id + " " + newOrder.username + " " +
                        newOrder.productName + " " + newOrder.quantity;
                    System.out.println(packetString);
                    //pout.println(packetString);
                } else if (successCode == 1) {
                    packetString = "Not Available - Not enough items";
                    System.out.println(packetString);
                    //pout.println(packetString);
                } else if (successCode == 2) {
                    packetString = "Not Available - We do not sell this product";
                    System.out.println(packetString);
                    //pout.println(packetString);
                }       
            } else if (tokens[0].equals("cancel")) {
                OrderTable.Order cancelledOrder = orders.delete(Integer.parseInt(tokens[1]));
                if (cancelledOrder.id != -1) {
                    System.out.println(cancelledOrder);
                    inventory.update(cancelledOrder.productName, cancelledOrder.quantity);
                    packetString = "Order " + cancelledOrder.id + " is cancelled";
                    System.out.println(packetString);
                    //pout.println(packetString);
                } else {
                    packetString = cancelledOrder.id + " not found, no such order";
                    System.out.println(packetString);
                    //pout.println(packetString);
                }
            } else if (tokens[0].equals("search")) {
                OrderTable userOrders = orders.searchByUser(tokens[1]);
                if (userOrders.table.size() == 0) {
                    packetString = "No order found for " + tokens[1];
                    System.out.println(packetString);
                    //pout.println(packetString);
                } else {
                    for (int i=0; i<userOrders.table.size(); i++) {
                        OrderTable.Order o = userOrders.table.get(i);
                        packetString = o.id + ",  " + o.productName + ", " + o.quantity;
                        System.out.println(packetString);
                        //pout.println(packetString);
                    }
                }
            } else if (tokens[0].equals("list")) {
                for(int i=0; i<inventory.table.size(); i++) {
                    InventoryTable.InventoryItem item = inventory.table.get(i);
                    packetString = item.name + " " + item.quantity;
                    System.out.println(packetString);
                    //pout.println(packetString);
                }
            }
         
            
            //UDP
            
            buf = packetString.getBytes();
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            this.theUdpClient.send(packet);
            this.theUdpClient.close();
            
            
            //TCP
            //pout.flush();
            theTcpClient.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}