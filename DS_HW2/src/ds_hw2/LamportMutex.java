
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
    int numInitResp = 0;
    int numSeats; // total number of seats. Required for init.
    SeatingTable seats;
    int timeoutTime;

    public LamportMutex(int myId, ServerTable serverDir, SeatingTable seats) {
        this.myId = myId;
        this.seats = seats;
        this.numSeats = seats.seatArray.length;
	this.c = new LamportClock(myId);
    this.neighbors = serverDir;
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
        this.timeoutTime = 100;
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
        numAcks = 0;
        int connectionWorked = 0;
        for (int i = 0; i < neighbors.serverList.length; i++) {
            if (i != this.myId) {
                try {
                    //this.getServerSocket(neighbors.serverList[i].hostAddress, neighbors.serverList[i].portNum);
                    this.otherServer = new Socket();
                    this.otherServer.connect(new InetSocketAddress(neighbors.serverList[i].hostAddress, neighbors.serverList[i].portNum), 100);
                    connectionWorked++;
                    pout = new PrintStream(this.otherServer.getOutputStream());
                    pout.println("request " + this.myId + " " + c.getValue());
                    pout.flush();
                    this.otherServer.close();
                } catch (SocketTimeoutException e) {
                    System.out.println("RequestCS: " + e);
                    numAcks++;
                } catch (IOException e){
                    System.out.println("RequestCS: " + e);
                }
            }
        }
	
        // TODO:  Not fault tolerant. Need timeout on this while loop
	    while ((q.peek().pid != myId) || (numAcks < connectionWorked)) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("LM1: " +e);
            }
	    }
    }
	
    public synchronized void releaseCS() {
        this.status[myId] = c.c;
	q.remove();
        //sendMsg(neighbors, "release", c.getValue());
        for (int i = 0; i < neighbors.serverList.length; i++) {
            if (i != this.myId) {
                try {
                    this.otherServer = new Socket();
                    this.otherServer.connect(new InetSocketAddress(neighbors.serverList[i].hostAddress, neighbors.serverList[i].portNum), timeoutTime);
                    pout = new PrintStream(this.otherServer.getOutputStream());
                    pout.println("release " + this.myId + " " + c.getValue());
                    pout.flush();
                    this.otherServer.close();
                } catch (SocketTimeoutException e){
                    System.out.println("ReleaseCS: " + e);
                } catch (IOException e) {
                    System.out.println("ReleaseCS: " + e);
                }
            }
        }
    }
    
    public synchronized void sendAllSeats() {
        for (int i = 0; i < neighbors.serverList.length; i++) {
            if (i != this.myId) {
                sendSeats(i);
            }
        }
    }
    private synchronized void sendSeats(int otherPid) {
        String mySeats = "";
        String eachSeat = "";
        for (int i = 0; i < this.numSeats; i++) {
            if (!this.seats.seatArray[i].available) {
                eachSeat = this.seats.seatArray[i].name + ":" +
                    this.seats.seatArray[i].available.toString();
            } else {
                eachSeat = ":" +
                    this.seats.seatArray[i].available.toString();
            }
            mySeats = mySeats + eachSeat + ",";
        }
        System.out.println("In sendSeats: " + mySeats);
        try {
            //this.getServerSocket(neighbors.serverList[otherPid].hostAddress, 
            //        neighbors.serverList[otherPid].portNum);
            this.otherServer = new Socket();
            this.otherServer.connect(new InetSocketAddress(neighbors.serverList[otherPid].hostAddress, 
                    neighbors.serverList[otherPid].portNum), timeoutTime);
            pout = new PrintStream(this.otherServer.getOutputStream());
            pout.println("respInit " + this.myId + " " + c.getValue() + " {" +
                    mySeats + "}");
            pout.flush();
            this.otherServer.close();
        } catch(SocketTimeoutException e){
            System.out.println("SendSeats" + e);
        }catch (IOException e) {
            System.out.println("SendSeats" + e);
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
            System.out.println("REQUESTED");
            q.add(new LamportClock(otherPid, otherLCV));
            //sendMsg(src, "ack",c.getValue());
            try {
                System.out.println("Trying to send ack to " + neighbors.serverList[otherPid].hostAddress + " : " + neighbors.serverList[otherPid].portNum);
                this.otherServer = new Socket();
                this.otherServer.connect(new InetSocketAddress(neighbors.serverList[otherPid].hostAddress, 
                        neighbors.serverList[otherPid].portNum), timeoutTime);
                pout = new PrintStream(this.otherServer.getOutputStream());
                System.out.println("sending ack to " + neighbors.serverList[otherPid].hostAddress + " : " + neighbors.serverList[otherPid].portNum);
                pout.println("ack " + this.myId + " " + c.getValue());
                pout.flush();
                this.otherServer.close();
            } catch(SocketTimeoutException e){
                System.out.println("HandleMsg" + e);
            } catch (IOException e) {
                System.out.println("HandleMsg" + e);
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
           System.out.println("HandleMsg respInit: " + command);
           String sub = command.substring(command.indexOf("{") + 1, command.indexOf("}"));
           System.out.println(sub);
           String[] mySeats = sub.split(", ");
           System.out.println(mySeats);
           this.seats = new SeatingTable(mySeats.length, mySeats);
           this.numInitResp++;
        }
        notifyAll();
    }
    
    public synchronized SeatingTable getInitial(Socket s) {
        c.tick();
        if (this.neighbors.serverList.length == 1) {
            return this.seats;
        }
        // this.status will have all the LCV's we have seen. Get largest
        int largest = -1;
        int largePid = -1;
        for (int i = 0; i < this.neighbors.serverList.length; i++) {
            if (status[i] > largest) {
                largest = status[i];
                largePid = i;
            }
        }
        boolean connected = false;
        try {
            //this.getServerSocket(neighbors.serverList[largePid].hostAddress, neighbors.serverList[largePid].portNum);
            this.otherServer = new Socket();
            System.out.println(neighbors.serverList[largePid].portNum);
            this.otherServer.connect(new InetSocketAddress(neighbors.serverList[largePid].hostAddress, 
                    neighbors.serverList[largePid].portNum), timeoutTime);
            connected = true;
            pout = new PrintStream(this.otherServer.getOutputStream());
            pout.println("init " + this.myId + " " + c.getValue());
            pout.flush();
            this.otherServer.close();
        } catch (IOException e) {
            System.out.println("LamportMutex:" + e);
        }
        // TODO:  Not fault tolerant. Need timeout on this while loop
        // Tyler: working on this
        if (connected) {
            this.numInitResp = 0;
	        while (this.numInitResp < 1) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println("LM2: " + e);
                }
            }
        }
        return this.seats;
    }
}
