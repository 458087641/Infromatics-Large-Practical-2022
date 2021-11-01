package uk.ac.ed.inf;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URI;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class Menus {
    private String name;
    private String port;
    private static final HttpClient client = HttpClient.newHttpClient();

    /**
     * constructor
     *
     * @param  name the name of the server
     * @param  port the port of the server
     */
    public Menus(String name, String port) {
        this.name = name;
        this.port = port;
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
        String uri = "http://"+this.name +":" + this.port +"/menus/menus.json";
        String response = getResponse(uri);
        Type listType =
                new TypeToken<ArrayList<menuJson>>() {
        }.getType();
        // Use the ”fromJson(String, Type)” method to get the list of menuJson objects.
        ArrayList<menuJson> menuj =
                new Gson().fromJson(response, listType);
        // go though all arguments, check the price stated in "menu.json", calculate the cost of the items in the input.
        for (String arg : varargs) {
            for (menuJson j : menuj) {
                for(menuJson.menu_list m : j.menu){
                    if (arg.equals(m.item)){
                        cost = cost+ m.pence;
                    }
                }
            }

        }
        return cost;
        }


    /**
     * helper function, used to get response from the server.
     *
     * @param  url the url to crate URI and used to get the server response
     * @return the body of the response from the server
     */
    public String getResponse(String url)  {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response =
                null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response.body();
        }
    }
