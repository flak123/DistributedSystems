import java.net.*;
import java.io.*;
import java.util.*;

public class RunnerThread extends Thread {
    Server ns;
    Socket s;
    String command;

    public RunnerThread(Server ns, Socket s, String command) {
        this.ns = ns;
        this.s = s;
        this.command = command;
    }

    public void run() {
        System.out.println("Waiting on cs...");
        this.ns.lamportMutex.requestCS(s);
        System.out.println("Inside CS!!!");
        try {
            Thread t = new ServerThread(this.ns.seats, this.s, this.command);
            t.start();
            t.join();
        } catch (Exception e) {
            System.out.println("RunnerThread: " + e);
        }
        this.ns.lamportMutex.sendAllSeats();
        this.ns.lamportMutex.releaseCS();
        System.out.println("CS RELEASED");
        // Status of system
        for(int seat=0; seat < this.ns.seats.seatArray.length; seat++) {
            System.out.println(seat + ":  "+  this.ns.seats.seatArray[seat].name);
        }
    }
}

