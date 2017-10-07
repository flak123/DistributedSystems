package DS_HW2;

import java.net.*;
import java.io.*;
import java.util.*;

public class ServerThread extends Thread {
    SeatingTable seats;
    Socket theClient;

    public ServerThread(SeatingTable seatingTable, Socket s, LamportClock lcv) {
        this.seats = seatingTable;
        this.theClient = s;
    }

    public void run() {
        String command;
        
        try {
            Scanner sc = new Scanner(theClient.getInputStream());
            PrintWriter pout = new PrintWriter(theClient.getOutputStream());
            command = sc.nextLine();
            
            System.out.println("received:" + command);
            String[] tokens = command.replaceAll("(\\r|\\n)", "").split(" ");
            
            Scanner st = new Scanner(command);          
            String tag = st.next();
           
            String packetString = "";
            //common
            if (tokens[0].equals("reserve")) {
                String bookName = tokens[1];
                int successCode = seats.reserveSeat(bookName);
                if (successCode >= 0) {
                    // success
                    packetString = "Seat assigned to you is " + 
                        successCode + "\n";
                } else if (successCode == -2) {
                    packetString = "Sold out - No seat available\n";
                } else if (successCode == -3) {
                    packetString = "Seat already booked against the name provided\n";
                }       
            } else if (tokens[0].equals("bookSeat")) {
                String bookName = tokens[1];
                int seatNum = Integer.parseInt(tokens[2]);
                int successCode = seats.bookSeat(bookName, seatNum);
                if (successCode >= 0) {
                    //success
                    packetString = "Seat assigned to you is " + 
                        successCode + "\n";
                } else if (successCode == -1) {
                    packetString = seatNum + " is not available\n";
                } else if (successCode == -3) {
                    packetString = "Seat already booked against the name provided\n";
                }
            } else if (tokens[0].equals("search")) {
                String bookName = tokens[1];
                int successCode = seats.searchSeat(bookName);
                if (successCode >= 0) {
                    //success
                    packetString = "Seat assigned to you is " + 
                        successCode + "\n";
                } else if (successCode == -4) {
                    packetString = "No reservation found for " + bookName + "\n";
                }
            } else if (tokens[0].equals("delete")) {
                String bookName = tokens[1];
                int successCode = seats.deleteSeat(bookName);
                if (successCode >= 0) {
                    //success
                    packetString = "Reservation cancelled for seat " 
                            + successCode + "\n";
                } else if (successCode == -4) {
                    packetString = "No reservation found for " + bookName + "\n";
                }
            }
            //System.out.println(packetString);
            pout.println(packetString);
         
            pout.flush();
            theClient.close();
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
