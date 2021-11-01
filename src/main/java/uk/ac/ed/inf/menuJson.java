package uk.ac.ed.inf;

import java.util.ArrayList;

//class for convert Json String to Java Object
public class menuJson {
    String name;
    String location;
    ArrayList<menu_list> menu;
    public static class menu_list {
        String item;
        int pence;
    }
}