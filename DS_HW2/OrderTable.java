
import java.net.*; import java.io.*; import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.*;
import java.io.*;
import java.util.stream.Stream;


public class OrderTable {
    public class Order {
        int id;
        String username;
        String productName;
        int quantity;

        public Order(int id, String username, String productName, int quantity) {
            this.id = id;
            this.username = username;
            this.productName = productName;
            this.quantity = quantity;
        }
    }
    
    ArrayList<Order> table = new ArrayList<Order>();
    int idCount = 0;
    
    public synchronized Order add(String username, String productName, int val) {
        this.idCount += 1;
        int orderId = this.idCount;
        Order o = new Order(orderId,username,productName,val);
        table.add(o);
        return o;
    }
    
    public synchronized OrderTable searchByUser(String username) {
        OrderTable userOrders = new OrderTable();
        for(Order o:this.table) {
            if (o.username.equals(username)) {
                userOrders.table.add(o);
            }
        }
        return userOrders;
    }
    
    public synchronized Order delete(int id) {
        for(Order o:this.table) {
            if (o.id == id) {
                this.table.remove(o);
                return o;
            }
        }
        Order fakeOrder = new Order(-1,"-1","-1",-1);
        return fakeOrder;
    }
}
