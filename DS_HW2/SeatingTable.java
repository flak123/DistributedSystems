package DS_HW2;


public class SeatingTable {
    private final Seat[] seatArray;
    private int capacity;
    
    public class Seat{
        private String name;
        private Boolean available;
        //private final int num;
            
        public Seat(int num){
            this.name = " ";
            this.available = true;
            //this.num = num;
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
    
    // no available seats: -2
    // user already booked seat: -3
    // seat sucsessfully booked: <positive seatNum>
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
            seatArray[num].updateSeat(" ");
            capacity--;
            return num + 1;
        }
        else{
            return num;
        }
    }
    
    public int checkPreviousReservations(String name){
        if(capacity == seatArray.length){
            return -2;
        }
        else if(searchSeat(name) != -1){
            return -3;
        }
        else{
            return 0;
        }
    }
    
}

