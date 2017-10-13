
public class SeatingTable {
    public final Seat[] seatArray;
    public int capacity;
    
    public class Seat{
        public String name;
        public Boolean available;
            
        public Seat(int num){
            this.name = " ";
            this.available = true;
        }
        
        public Seat(String name, boolean available){
            this.name = name;
            this.available = available;
        }
        
        public void updateSeat(String name){
            this.name = name;
            this.available = !this.available;
        }
    }

    public SeatingTable(int numberOfSeats){
       this.seatArray = new Seat[numberOfSeats];
       for(int i = 0; i < numberOfSeats; i++){
           this.seatArray[i] = new Seat(i);
       }
       this.capacity = 0;
    }
    
    public SeatingTable(int numberOfSeats, String[] existing){
       this.seatArray = new Seat[numberOfSeats];
       this.capacity = 0;
       System.out.println("in SeatingTable constructor");
       for(int i = 0; i < numberOfSeats; i++){
           String[] seatData = existing[i].split(":");
           System.out.println(seatData[0]);
           System.out.println(seatData[1]);
           boolean av = Boolean.parseBoolean(seatData[1]);
           Seat thisSeat = new Seat(seatData[0], av);
           if (!this.seatArray[i].available) {
               this.capacity++;
           }
       }
    }
    
    // no available seats: -2
    // user already booked seat: -3
    // seat successfully booked: <positive seatNum>
    public int reserveSeat(String name){
        int check = checkPreviousReservations(name);
        if (check != 0){
            return check;
        }
        for(int i = 0; i < seatArray.length; i++){
            if(seatArray[i].available){
                seatArray[i].updateSeat(name);
                return i + 1;
            }
        }
        return check;
    }
    
    // user already booked seat: -3
    // seat not available: -1
    // seat successfully booked: positive <seatNum>
    public int bookSeat(String name, int num){
        int alreadyBooked = searchSeat(name);
        if (alreadyBooked != -4) {
            return -3;
        }
        if(seatArray[num - 1].available){
            seatArray[num - 1].updateSeat(name);
            capacity++;
            return num;
        }
        return -1;
    }
    
    public int searchSeat(String name){
        for(int i =0; i < seatArray.length; i++){
            if(name.equals(seatArray[i].name)){
                return i + 1;
            }
        }
        return -4;
    }
    
    // if user does not have booking: -4
    // successful delete: positive <seatNum>
    public int deleteSeat(String name){
        int num = searchSeat(name);
        if(num != -4){
            seatArray[num-1].updateSeat("");
            capacity--;
            return num;
        }
        else{
            return num;
        }
    }
    
    public int checkPreviousReservations(String name){
        if(capacity == seatArray.length){
            return -2;
        }
        else if(searchSeat(name) != -4){
            return -3;
        }
        else{
            return 0;
        }
    }
    
}

