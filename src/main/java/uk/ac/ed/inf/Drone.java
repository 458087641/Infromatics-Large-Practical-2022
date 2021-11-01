package uk.ac.ed.inf;

public class Drone {
    private int batteryLife;
    private LongLat position;

    /**\
     * constructor
     *
     */
    public Drone(){
        // drones always start from top of appleton tower.
        this.releaseFromApp();
        // at the beginning, the drone is fully charged, has 1500 available movement.
        this.batteryLife = 1500;
    }


    public void releaseFromApp(){
       this.position.longitude = -3.186874;
       this.position.latitude= 55.944494;
    }

    public void chargeBattery() {
        this.batteryLife = 1500;
    }

    /**
     * drones collect the food, by hovering once.
     */
    public void collectFood(){
        droneMove(-999);
    }

    /**
     * drone deliever the food, by hovering once.
     */
    public void deliverFood(){
        droneMove(-999);
    }

    /**
     * drone movement, including fly and hovering.
     * @param angle direction of movement
     * @return true if move succeed
     */
    public boolean droneMove(int angle){
         if (this.batteryLife <= 0){
             return false;
         }
        try {
            // update the position after move
            this.position = this.position.nextPosition(angle);
            // update the batterylife by -1
            this.batteryLife = this.batteryLife - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;

    }
}
