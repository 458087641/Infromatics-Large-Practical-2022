package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;


import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * main to run the program, and write result into geojson file.
 */
public class App {
    public static void main( String[] args ) {
        long begin = System.nanoTime();
        String day = args[0];
        String month = args[1];
        String year = args[2];
        String httpPort = args[3];
        String databasePort = args[4];


        String date = year + "-" + month + "-" + day;
        Drone d = new Drone(date,httpPort,databasePort);
        if (!d.db.connectToDB()){
            System.out.println("cannot connect to database");
            System.exit(1);
        }
        d.db.checkDeliveries();
        d.db.checkFlightpath();
        d.start();
        ArrayList<LongLat> pathLongLat = d.getAllPath();
        ArrayList<Point>path=new ArrayList<>();
        for (LongLat l : pathLongLat){
            path.add(Point.fromLngLat(l.longitude,l.latitude));
        }
        LineString lineString = LineString.fromLngLats(path);
        Feature flightPathFeature = Feature.fromGeometry(lineString);
        FeatureCollection collection = FeatureCollection.fromFeature(flightPathFeature);
        String buffer = collection.toJson();
        String dronePathFile = "drone-" +day + "-" + month + "-" + year +".geojson";
        try {
            PrintWriter writer = new PrintWriter(dronePathFile);
            writer.println(buffer);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        long end = System.nanoTime();
        System.out.println("All Time is  " + (end - begin) / 1000000000.0 + " seconds");

    }
}
