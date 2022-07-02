package uk.ac.ed.inf;
import com.mapbox.geojson.Point;

import java.util.List;
/**
 * JSON structure for location of non-fly buildings in http server
 */
public class NonFlyZone {

    private String name;
    private List<Point> coordinates;

    /**
     * constructor
     * @param name name of non-fly building
     * @param coordinates non-fly building's border coordinates
     */
    public NonFlyZone(String name, List<Point> coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    /**
     * get the name of non-fly building
     * @return name of non-fly building
     */
    public String getName() {
        return name;
    }

    /**
     * get the border coordinates of non-fly building
     * @return border coordinates of non-fly building
     */
    public List<Point> getCoordinates() {
        return coordinates;
    }
}
