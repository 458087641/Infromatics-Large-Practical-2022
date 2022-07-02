package uk.ac.ed.inf;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.*;

import java.io.IOException;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

/**
 * class contain methods interacting with http server, used to gte and parse JSON and GeoJson requested from http server
 */
public class ServerConnection {
    private String port;
    private String server;
    private String jsonString;
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * constructor
     * @param port port to connect to http server
     * @param server localhost
     */
    public ServerConnection(String port, String server){
        this.port = port;
        this.server = server;
    }

    /**
     * return url for server request
     * @return url for server request
     */
    public String getURL(){
        return "http://" + this.server + ":" + this.port;
    }

    /**
     * return the json response form server, in the most recent request
     * @return json response form server
     */
    public String getJson(){
        return this.jsonString;
    }

    /**
     * get the response from server with url as parameter, store the response body to this.jsonstring.
     * @param url url to get server response
     */
    public void getServerResponse(String url){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<String> response =client.send(request, BodyHandlers.ofString());

            if (response.statusCode()== 200){
                this.jsonString=response.body();
            }
            else{
                System.out.println("request fail: Response code: "
                        + response.statusCode() + " for '" + url + "'");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * generate url for given w3w
     * @param word seprate word to get url
     * @return url for given w3w
     */
    public String generateUrlForLocation(String word){
        String[] words = word.split("[.]");
        String url = getURL() +"/words/"+ words[0] + "/" + words[1] + "/" + words[2] + "/details.json";
        return url;
    }

    /**
     * get geojson file from server, parse the geojson file to get list of non fly buildings
     * @return List of all non fly buildings in the geojson file.
     */
    public ArrayList<NonFlyZone> getNoFlyBuildings(){
        ArrayList<NonFlyZone> buildings = new ArrayList<>();
        this.getServerResponse(this.getURL() + "/buildings/no-fly-zones.geojson");
        FeatureCollection fc = FeatureCollection.fromJson(this.jsonString);
        List<Feature> fs = fc.features();
        for(Feature f : fs){
            Geometry g =f.geometry();
            Polygon p = (Polygon)g;
            ArrayList<Point> coordinates = new ArrayList<Point>();
            for (Point j : p.coordinates().get(0)) {
                coordinates.add(j);
            }
            NonFlyZone building = new NonFlyZone(f.properties().get("name").getAsString(),coordinates);
            buildings.add(building);
        }
        return buildings;
    }

    /**
     * get the landmarks from http server.
     * @return Arraylist of all the landmarks as class LandMsrks
     */
    public ArrayList<LandMarks> getLandMarks(){
        ArrayList<LandMarks> landMarks = new ArrayList<>();
        this.getServerResponse(this.getURL() + "/buildings/landmarks.geojson");
        FeatureCollection fc = FeatureCollection.fromJson(this.jsonString);
        List<Feature> fs = fc.features();
        for(Feature f : fs){
            Geometry g =f.geometry();
            Point p = (Point)g;
            LandMarks landmark = new LandMarks(f.properties().get("name").toString(),p);
            landMarks.add(landmark);
        }
        return landMarks;
    }

    /**
     * get the LongLat object, i.e. position of w3w
     * @param word the string contain w3w word, used to fin location
     * @return LongLat object contain position of the w3w string.
     */
    public LongLat getWhat3WordsLocation(String word){
        String url = generateUrlForLocation(word);
        try{
            this.getServerResponse(url);
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        LongLat longLat = null;
        try {
            W3WLocation location  = new Gson().fromJson(this.jsonString, W3WLocation.class);
            W3WLocation.Coordinates coordinates = location.coordinates;
            longLat = new LongLat(coordinates.lng,coordinates.lat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return longLat;
    }

    /**
     * find out the total cost of delivery, including the items' price ana delivery fee.
     *
     * @param  varargs the input of items that the user want the drones to buy, with varies number of arguments
     * @return calculated total cost
     */
    public int getDeliveryCost(String... varargs) {
        // ensure the input length larger than 0 smaller than 4
        if (varargs.length==0){
            return 0;
        }
        if (varargs.length>4){
            throw new IllegalArgumentException();
        }
        // the initial value of cost is 50p, representing the delivery fee.
        int cost = 50;
        // do the http request to get the menu.json
        String uri = "http://"+this.server +":" + this.port +"/menus/menus.json";
        this.getServerResponse(uri);
        Type listType =
                new TypeToken<ArrayList<Shop>>() {
                }.getType();
        // Use the "fromJson(String, Type)"method to get the list of menuJson objects.
        ArrayList<Shop> menuj =
                new Gson().fromJson(this.jsonString, listType);
        // go though all arguments, check the price stated in "menu.json", calculate the cost of the items in the input.
        for (String arg : varargs) {
            for (Shop j : menuj) {
                for(Shop.menu_list m : j.getMenu()){
                    if (arg.equals(m.item)){
                        cost = cost+ m.pence;
                    }
                }
            }

        }
        return cost;
    }

    /**
     * get all shops from http server.
     * @return Arraylist of shops contain all shops in server
     */
    public ArrayList<Shop> getMenus(){
        String uri = "http://"+this.server +":" + this.port +"/menus/menus.json";
        this.getServerResponse(uri);
        Type listType =
                new TypeToken<ArrayList<Shop>>() {
                }.getType();
        // Use the "fromJson(String, Type)" method to get the list of menuJson objects.
        ArrayList<Shop> menuj =
                new Gson().fromJson(this.jsonString, listType);
        return menuj;
    }


}
