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
    Socket otherServer; // socket to communicate with other servers
    Scanner din;
    PrintStream pout;
    ServerTable neighbors;

    public LamportMutex(int myId, ServerTable serverDir) {
        this.myId = myId;
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
		// TODO: neighbors in Server.Java
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
	while ((q.peek().pid != myId) || (numAcks < neighbors.serverList.length-1)) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println(e);
            }
	}
    }
	
    public synchronized void releaseCS() {
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
	
    public synchronized void handleMsg(String command) {
        // command in format: <action> <pid> <lcv>
        String[] tokens = command.split(" ");
        String tag = tokens[0];
        int otherPid = Integer.parseInt(tokens[1]);
        int otherLCV = Integer.parseInt(tokens[2]);
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
        }
    notifyAll();
    }
}
