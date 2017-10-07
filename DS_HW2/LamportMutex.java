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

    public LamportMutex(int myId) {
        this.myId = myId;
	c = new LamportClock(myId);
	q = new PriorityQueue<LamportClock>(n, 	
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
        for (int i = 0; i < neighbors.length(); i++) {
            this.getServerSocket(neighbors[i].host, neighbors[i].port);
            pout.flush("request " + myID + " " + c.getValue());
        }
		numAcks = 0;
	while ((q.peek().pid != myId) || (numAcks < neighbors.length()-1)) {
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
        for (int i = 0; i < neighbors.length; i++) {
            this.getServerSocket(neighbors[i].host, neighbors[i].port);
            pout.flush("release " + this.myID + " " + c.getValue());
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
            q.add(new LamportClock(otherPID, otherLCV));
            //sendMsg(src, "ack",c.getValue());
            // TODO: We have to look src up in neighbor dir? Pass in?
            for (int i = 0; i < neighbors.length; i++) {
                if (neighbors[i].pid == otherPid) {
                   this.getServerSocket(neighbors[i].host, neighbors[i].port);
                   pout.flush("ack " + this.myID + " " + c.getValue());
                }
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
