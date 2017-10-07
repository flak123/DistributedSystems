package DS_HW2;

public class LamportClock {
    int pid;
    int c;
    
    public LamportClock(int pid) {
        this.pid = pid;
        this.c = 1;
    }

    public LamportClock(int pid, int startVal) {
        this.pid = pid;
        this.c = startVal;
    }
    
    public synchronized int getValue() {
        return c;
    }

    public synchronized int getPid() {
        return pid;
    }
    
    public synchronized void tick() { // on internal events
        this.c = this.c + 1;
    }
    
    public synchronized void sendAction() {
       // include c in message
        this.c = this.c + 1;      
    }
    
    public synchronized void receiveAction(int otherC) {
        this.c = Math.max(this.c, otherC) + 1;
    }
}
