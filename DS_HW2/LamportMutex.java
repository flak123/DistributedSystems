package DS_HW2;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.Scanner;

public class LamportMutex {
    LamportClock c;
    int numAcks;
    int myId;
    Queue<LamportClock> q; // request queue
    int[] status; // keeps LVC of all other servers
    Socket otherServer; // socket to communicate with other servers
    Scanner din;
    PrintStream pout;
    ServerTable neighbors;
    String[] initSeats;
    int numInitResp;
    int numSeats; // total number of seats. Required for init.
    SeatingTable seats;

    public LamportMutex(int myId, ServerTable serverDir, SeatingTable seats) {
        this.myId = myId;
        this.seats = seats;
        this.numSeats = seats.seatArray.length;
	this.c = new LamportClock(myId);
	this.q = new PriorityQueue<LamportClock>(this.neighbors.serverList.length, 	
            new Comparator<LamportClock>() {
                public int compare(LamportClock a, LamportClock b) {
                    if (a.getValue() > b.getValue()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        );
        this.status = new int[this.neighbors.serverList.length];
        this.status[myId] = c.c;
	numAcks = 0;
        this.neighbors = serverDir;
    }

    public void getServerSocket(String host, int port) throws IOException {
        this.otherServer = new Socket(host, port);
        this.pout = new PrintStream(this.otherServer.getOutputStream());
        this.din = new Scanner(this.otherServer.getInputStream());
    }

    public synchronized void requestCS(Socket s) {
	c.tick();
	q.add(c);
        this.status[myId] = c.c;
        for (int i = 0; i < neighbors.serverList.length; i++) {
            try {
                this.getServerSocket(neighbors.serverList[i].hostAddress, neighbors.serverList[i].portNum);
                pout.println("request " + this.myId + " " + c.getValue());
                pout.flush();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
	numAcks = 0;
        // TODO:  Not fault tolerant. Need timeout on this while loop
	while ((q.peek().pid != myId) || (numAcks < neighbors.serverList.length-1)) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
	}
    }
	
    public synchronized void releaseCS() {
        this.status[myId] = c.c;
	q.remove();
        //sendMsg(neighbors, "release", c.getValue());
        for (int i = 0; i < neighbors.serverList.length; i++) {
            try {
                this.getServerSocket(neighbors.serverList[i].hostAddress, neighbors.serverList[i].portNum);
                pout.println("release " + this.myId + " " + c.getValue());
                pout.flush();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
    
    public synchronized void sendAllSeats() {
        for (int i = 0; i < neighbors.serverList.length; i++) {
            sendSeats(i);
        }
    }
    private synchronized void sendSeats(int otherPid) {
        String[] mySeats = new String[numSeats];
            for (int i = 0; i < this.numSeats; i++) {
                mySeats[i] = this.seats.seatArray[i].name + ":" +
                        this.seats.seatArray[i].available.toString();
            }
            try {
                this.getServerSocket(neighbors.serverList[otherPid].hostAddress, 
                        neighbors.serverList[otherPid].portNum);
                pout.println("respInit " + this.myId + " " + c.getValue() + 
                        mySeats.toString());
                pout.flush();
            } catch (IOException e) {
                System.out.println(e);
            }
    }
    public synchronized void handleMsg(String command) {
        // command in format: <action> <pid> <lcv>
        String[] tokens = command.split(" ");
        String tag = tokens[0];
        int otherPid = Integer.parseInt(tokens[1]);
        int otherLCV = Integer.parseInt(tokens[2]);
        this.status[otherPid] = otherLCV;
        c.receiveAction(otherLCV);
        if (tag.equals("request")) {
            q.add(new LamportClock(otherPid, otherLCV));
            //sendMsg(src, "ack",c.getValue());
            try {
                this.getServerSocket(neighbors.serverList[otherPid].hostAddress, neighbors.serverList[otherPid].portNum);
                pout.println("ack " + this.myId + " " + c.getValue());
                pout.flush();
            } catch (IOException e) {
                System.out.println(e);
            }
        } else if (tag.equals("release")) {
            Iterator<LamportClock> it =  q.iterator();			    
            while (it.hasNext()){
                if (it.next().getPid() == otherPid) {
                    it.remove();
                }
            }
        } else if (tag.equals("ack")) {
            numAcks++;    
        } else if (tag.equals("init")) {
            // send seats over
            sendSeats(otherPid);
        } else if (tag.equals("respInit")) {
           // set mySeats
           String[] mySeats = command.substring(command.indexOf("[") + 1, command.indexOf("]")).split(", ");
           this.seats = new SeatingTable(mySeats.length, mySeats);
           this.numInitResp++;
        }
        notifyAll();
    }
    
    public synchronized SeatingTable getIntital(Socket s) {
        c.tick();
        // this.status will have all the LCV's we have seen. Get largest
        int largest = -1;
        int largePid = -1;
        for (int i = 0; i < this.neighbors.serverList.length; i++) {
            if (status[i] > largest) {
                largest = status[i];
                largePid = i;
            }
        }
        try {
            this.getServerSocket(neighbors.serverList[largePid].hostAddress, neighbors.serverList[largePid].portNum);
            pout.println("init " + this.myId + " " + c.getValue());
            pout.flush();
        } catch (IOException e) {
            System.out.println(e);
        }
        // TODO:  Not fault tolerant. Need timeout on this while loop
        this.numInitResp = 0;
	while (this.numInitResp < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
        }
        return this.seats;
    }
}
