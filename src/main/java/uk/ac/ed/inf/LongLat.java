package uk.ac.ed.inf;
import java.lang.Math.*;

public class LongLat
{
    public double longitude;
    public double latitude;

    /**
     * Constructor
     * @param longitude longitude for representing position
     * @param latitude latitude for representing position
     */

    public LongLat(double longitude, double latitude){
        this.latitude =latitude;
        this.longitude = longitude;
    }
    /**
     * check weather the drone is in the confinement area
     *
     * @return true for drone is in the confinement area, flase for not
     */
    public boolean isConfined(){
        if (((this.longitude >= -3.192473) && (this.longitude <= -3.184319)) &&
                ((this.latitude >= 55.942617) && (this.latitude <= 55.946233))) {
            return true;
        }
        return false;
    }

    /**
     * check the distance between current LongLat object and a given LongLat object
     *
     * @param obj LongLat object that need to calculate the distance
     * @return distance between parameter obj and object itself
     */
    public double distanceTo(LongLat obj){
        //calculate the difference of longitude.
        double log_diff = this.longitude - obj.getLongitude();
        //calculate the difference of latitude.
        double lat_diff = this.latitude - obj.getLatitude();
        // calculate the Pythagorean distance.
        double diff = Math.sqrt(Math.pow(log_diff,2) + Math.pow(lat_diff,2));
        return diff;
    }

    /**
     * check the given LongLat object is close to current LongLat object or not
     *
     * @param obj LongLat object that need to be found close to or not
     * @return ture if the object itself is close to parameter obj
     */
    public boolean closeTo(LongLat obj){
        double distance = this.distanceTo(obj);
        if (distance < 0.00015) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * given an angle direction calculate the position of the drone after the move
     *
     * @param angle the direction of the movement
     * @return a LongLat object that indicate the position after move in the given direction
     */
    public LongLat nextPosition(int angle)  {
        // angle = -999 means hover and do not change position.
        if (angle == -999){
            LongLat new_LongLat = new LongLat(this.longitude, this.latitude);
            return new_LongLat;
        }
        // the input angle must be the multiple of 10.
        if (angle>360 & angle % 10 != 0 & angle<0) {
            throw new IllegalArgumentException("input must be multiples of 10");
        }
        // convert angle to radians for math.sin/cos to calculate.
        double radians = Math.toRadians(angle);
        // use cosine law and sin law to calculate the change in longitude and latitude.
        double changed_long = (0.00015 * Math.cos(radians)) + this.longitude;
        double changed_lat = (0.00015 * Math.sin(radians)) + this.latitude;
        LongLat new_LongLat = new LongLat(changed_long, changed_lat);
        return new_LongLat;
    }

    /**
     * return the Latitude of the LongLat object
     * @return Latitude of the LongLat object
     */
    public double getLatitude(){
        return this.latitude;
    }

    /**
     * return the Longitude of the LongLat object
     * @return Longitude of the Longlat object
     */
    public double getLongitude(){
        return this.longitude;
    }
}
