package distributedsystemshw1;

import java.net.*; import java.io.*; import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.*;
import java.io.*;
import java.util.stream.Stream;

public class InventoryTable {
    public class InventoryItem {
        String name;
        int quantity;

        public InventoryItem (String name, String q) {
            this.name = name;
            this.quantity = Integer.parseInt(q);
        }

        void update (int value) {
            this.quantity += value;
        }
    }
    
    ArrayList<InventoryItem> table = new ArrayList<InventoryItem>();
    
    public InventoryTable(String fileName) {
        Path path = Paths.get(fileName);
        try(Stream<String> lines = Files.lines(path)){
            lines.forEach(
                line ->  {
                    String [] item = line.split(" ");
                    InventoryItem itemObject = new InventoryItem(item[0],item[1]);
                    this.table.add(itemObject);
                }
            );
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // 0 = success
    // 1 = not enough quantity
    // 2 = product does not exist
    public synchronized Integer update(String name, int val) {
        for (InventoryItem item: table) {
            if (item.name.equals(name)) {
                if (item.quantity >= val) {
                    item.update(-1*val);
                    return 0;
                } else {
                    return 1;
                 }
            }
        };
        return 2;
    }
    
}
