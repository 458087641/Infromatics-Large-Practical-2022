package uk.ac.ed.inf;
/**
 * JSON structure for location of w3w location
 */
public class W3WLocation {
    public String country;
    public Sqaure square;
    public class Sqaure {
        Southwest southwest;
        NorthWest northwest;

        public class Southwest {
            double lng;
            double lat;
        }
        public class NorthWest {
            double lng;
            double lat;
        }
    }
    public String nearestPlace;
    public Coordinates coordinates;
    public String words;
    public String language;
    public String map;
    public class Coordinates {
        double lng;
        double lat;
    }

}
