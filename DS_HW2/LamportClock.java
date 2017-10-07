package DS_HW2;

public class LamportClock {
    int c;
    
    public LamportClock() {
        c = 1;
    }
    
    public synchronized int getValue() {
        return c;
    }
    
    public synchronized void tick() { // on internal events
        c = c + 1;
    }
    
    public synchronized void sendAction() {
       // include c in message
        c = c + 1;      
    }
    
    public synchronized void receiveAction(int src, int sentValue) {
        c = Math.max(c, sentValue) + 1;
    }
}
