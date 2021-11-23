package uk.ac.ed.inf;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;

import java.util.List;


public class NonFlyZone {
    private String name;
    private List<Feature> noneFlyBuildings;


    public String getName(){return this.name;}
    public List<Feature> getNoneFlyBuildings() {
        return this.noneFlyBuildings;
    }
}
