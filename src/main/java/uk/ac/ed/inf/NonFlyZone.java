package uk.ac.ed.inf;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.util.List;


public class NonFlyZone {
    private List<Feature> noneFlyBuildings;
    private ServerConnection connection;

    public void requestNonFlyZone(){
        this.connection.getServerResponse(connection.getURL() + "/buildings/no-fly-zones.geojson");
        this.noneFlyBuildings = FeatureCollection.fromJson(this.connection.getJson()).features();
    }

    public List<Feature> getNoneFlyBuildings() {
        return noneFlyBuildings;
    }
}
