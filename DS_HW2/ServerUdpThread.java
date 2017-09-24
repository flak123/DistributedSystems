
import java.net.*; import java.io.*; import java.util.*;
public class ServerUdpThread extends Thread {
    InventoryTable inventory;
    OrderTable orders;
    DatagramSocket theClient;
    //DatagramPacket datapacket, returnpacket;
    int len = 1024;

    public ServerUdpThread(InventoryTable inventory, OrderTable orderTable, DatagramSocket s) {
        this.inventory = inventory;
        this.orders = orderTable;
        this.theClient = s;
        //this.socket = new DatagramSocket(udpPort);
        //this.in = new BufferedReader();
    }

    class SortByName implements Comparator<InventoryTable.InventoryItem> {
        public int compare(InventoryTable.InventoryItem a, InventoryTable.InventoryItem b)
        {
            return a.name.compareTo(b.name);
        }
    }

    public void run() {
        String command;
        
        try {
            //UPDP
            
            byte[] buf = new byte[len];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.theClient.receive(packet);
            command = new String(packet.getData(),0, packet.getLength());
            
            //System.out.println("received:" + command);
            String[] tokens = command.replaceAll("(\\r|\\n)", "").split(" ");
            
            //UDP
            String packetString = "";            
            //common
            if (tokens[0].equals("purchase")) {
                int quantity = Integer.parseInt(tokens[3]);
                int successCode = inventory.update(tokens[2], quantity);
                if (successCode == 0) {
                    OrderTable.Order newOrder = orders.add(tokens[1], tokens[2], Integer.parseInt(tokens[3]));
                    packetString = "Your order has been placed, " + 
                        newOrder.id + " " + newOrder.username + " " +
                        newOrder.productName + " " + newOrder.quantity + "\n";
                } else if (successCode == 1) {
                    packetString = "Not Available - Not enough items\n";
                } else if (successCode == 2) {
                    packetString = "Not Available - We do not sell this product\n";
                }       
            } else if (tokens[0].equals("cancel")) {
                OrderTable.Order cancelledOrder = orders.delete(Integer.parseInt(tokens[1]));
                if (cancelledOrder.id != -1) {
                    System.out.println(cancelledOrder);
                    inventory.update(cancelledOrder.productName, (-1*cancelledOrder.quantity));
                    packetString = "Order " + cancelledOrder.id + " is cancelled\n";
                } else {
                    packetString = cancelledOrder.id + " not found, no such order\n";
                }
            } else if (tokens[0].equals("search")) {
                OrderTable userOrders = orders.searchByUser(tokens[1]);
                if (userOrders.table.size() == 0) {
                    packetString = "No order found for " + tokens[1] + "\n";
                } else {
                    for (int i=0; i<userOrders.table.size(); i++) {
                        OrderTable.Order o = userOrders.table.get(i);
                        packetString += o.id + ",  " + o.productName + ", " + o.quantity + "\n";
                    }
                }
            } else if (tokens[0].equals("list")) {
                InventoryTable.InventoryItem [] items = new InventoryTable.InventoryItem [inventory.table.size()];
                for(int i=0; i<inventory.table.size(); i++) {
                    InventoryTable.InventoryItem item = inventory.table.get(i);
                    items[i] = item;
                }
                Arrays.sort(items, new SortByName());
                for (InventoryTable.InventoryItem item : items) {
                    packetString += item.name + " " + item.quantity + "\n";
                }
            }
            //System.out.println(packetString);
            
            //UDP
            byte[] respBuf = new byte[len]; 
            respBuf = packetString.getBytes();
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            DatagramPacket respPacket = new DatagramPacket(respBuf, respBuf.length, address, port);
            this.theClient.send(respPacket);
            //this.theClient.close();

        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
