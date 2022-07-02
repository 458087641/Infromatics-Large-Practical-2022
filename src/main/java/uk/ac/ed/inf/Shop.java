package uk.ac.ed.inf;

import java.util.ArrayList;
/**
 * JSON structure for location of shops in menu.json
 */

public class Shop {
    private String name;
    private String location;
    private ArrayList<menu_list> menu;

    /**
     * constructor
     */
    public static class menu_list {
        String item;
        int pence;
    }

    /**
     * get the W3W location of the shop
     * @return W3W location String of the shop
     */
    public String getW3WLocation() {
        return location;
    }

    /**
     * get name of the shop
     * @return name of the shop
     */
    public String getName() {
        return name;
    }

    /**
     * get menu of the shop
     * @return menu of the shop
     */
    public ArrayList<menu_list> getMenu() {
        return menu;
    }

}