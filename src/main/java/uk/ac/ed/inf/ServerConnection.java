package uk.ac.ed.inf;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.io.IOException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.List;

public class ServerConnection {
    private String port;
    private String server;
    private String jsonString;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public ServerConnection(String port, String server){
        this.port = port;
        this.server = server;
    }
    public String getURL(){
        return "http://" + this.server + ":" + this.port;
    }
    public String getJson(){
        return this.jsonString;
    }

    public void getServerResponse(String url){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        try {
            HttpResponse<String> response =CLIENT.send(request, BodyHandlers.ofString());

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
    public List<Feature> requestNonFlyZone(){
        List<Feature> noneFlyBuildings;
        this.getServerResponse(this.getURL() + "/buildings/no-fly-zones.geojson");
        noneFlyBuildings = FeatureCollection.fromJson(this.getJson()).features();
        return noneFlyBuildings;
    }
}
