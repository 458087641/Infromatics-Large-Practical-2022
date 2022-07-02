package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
/**
 * JSON structure for location of landmarks in http server
 */
public class LandMarks {
    private String name;
    private Point location;

    /**
     * constructor
     * @param name name of landmark
     * @param location location of the landmark
     */
    public LandMarks(String name, Point location){
        this.name=name;
        this.location=location;

    }

    /**
     * get the name of the Landmark
     * @return name of the landmark
     */
    public String getName(){return this.name;}

    /**
     * get the coordinate of the landmark
     * @return coordinate of the landmark
     */
    public Point getLocation() {
        return this.location;
    }
}
