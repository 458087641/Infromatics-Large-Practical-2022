package uk.ac.ed.inf;

import com.mapbox.geojson.Point;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * class indicating a drone, used to plan the drone fly path of a give day
 */
public class Drone {
    // variable indicating remaining moves can be done by the drone.
    private int batteryLife;
    // variable indicating the position of the drone.
    private LongLat position;
    // list of shops that the drone aiming to go
    private ArrayList<ShopLonglat> targetShops;
    // the order that the drone will perform a deliver.
    private Orders nextOrder;
    // all shops with their coordinates.
    private ArrayList<ShopLonglat> shops = new ArrayList<>();
    // orders that has not been delivered.
    private ArrayList<Orders> remainOrders = new ArrayList<>();
    // list save all the details of non-fly-buildings
    private ArrayList<NonFlyZone> nonFlyZone= null;
    // position of appleton tower, for initialize start position and flay back
    private LongLat startpos =new LongLat(-3.186874,55.944494);
    /**
     * database instance, for using function in class databasefunctions.
     */
    public DatabaseFunctions db = null;
    //http instance, for using functions in ServerConnection class
    private ServerConnection server =null;
    // store all landmarks
    private ArrayList<LandMarks> landmarks= null;
    // store all the position that drone visited, indicating path of the drone
    private ArrayList<LongLat> allPath=new ArrayList<LongLat>();
    //store angles for current path.
    ArrayList<Integer> angles= new ArrayList<Integer>();
    // class allow Shop class add its coordinates
    int hovercount= 0;
    private static class ShopLonglat{
        private Shop shop;
        private LongLat location;
    }
    /**\
     * constructor
     * initialize by setting values from parameters and request data from server and database.
     * @param date the date that drone will perform delivery
     * @param httpPort the port of http server
     * @param databasePort the port of database
     */
    public Drone(String date, String httpPort, String databasePort){
        this.position = startpos;
        this.batteryLife = 1500;
        this.server= new ServerConnection(httpPort,"localhost");
        this.db=new DatabaseFunctions(databasePort,"localhost");
        for (Shop s:server.getMenus()){
            ShopLonglat shopLonglat=new ShopLonglat();
            shopLonglat.shop=s;
            shopLonglat.location= server.getWhat3WordsLocation(s.getW3WLocation());
            this.shops.add(shopLonglat);
        }
        this.remainOrders = db.getOrderByDate(date);
        this.nonFlyZone = server.getNoFlyBuildings();
        // assign shops its LongLat location get from w3w
        this.landmarks = server.getLandMarks();
        for (Orders o : this.remainOrders){
            o.setDeliverTo(server.getWhat3WordsLocation(o.getDeliverToW3W()));
        }
    }

    /**
     * find shops that have items in the order
     * @param order Order object that need to find shops contains its items
     * @return arraylist of shops that contian items of the order.
     */
    private ArrayList<ShopLonglat> fetchShops(Orders order){
        ArrayList<ShopLonglat> shops = new ArrayList<>();
        for (String item : order.getItem()) {
            for (ShopLonglat s : this.shops) {
                for (Shop.menu_list menu : s.shop.getMenu()) {
                    if (item.equals(menu.item)) {
                        if(!shops.contains(s)){
                            shops.add(s);
                        }
                    }
                }
            }
        }
        return shops;
    }

    /**
     * find the order that have highest ratio between order price and flay distance. chose it as the next order to deliver
     */
    private void getNextMenuAndOrder() {
        ArrayList<ShopLonglat> bestShop = null;
        Orders bestOrder = null;
        double bestMonetaryValue = -1;
        // go though all not-delivered order to find the next order to deliver.
        for (Orders order : this.remainOrders) {
            ArrayList<ShopLonglat> shops = fetchShops(order);
            order.setPrice(this.server.getDeliveryCost(order.getItem().toArray(new String[order.getItem().size()])));
            // find total distance between locations that need to visit in current order
            double distance1 = order.getDeliverTo().distanceTo(this.shops.get(shops.size()-1).location);
            double distance2 = position.distanceTo(this.shops.get(0).location);
            double interDistance =0.0;
            if (shops.size()>1){
                for (int i =0; i<shops.size()-1; i++){
                    interDistance=interDistance+this.shops.get(i).location.distanceTo(this.shops.get(i+1).location);
                }
            }
            // calculate the ratio between order value and flay distance
            double currentMonetaryValue = order.getPrice() / (distance2 + distance1+interDistance);
            if (currentMonetaryValue > bestMonetaryValue) {
                bestMonetaryValue = currentMonetaryValue;
                bestShop = shops;
                bestOrder = order;
            }
        }
        // decide next order to deliver, remove that order from not delivered order list.
        this.remainOrders.remove(bestOrder);
        this.targetShops = bestShop;
        this.nextOrder = bestOrder;
    }

    /**
     * if the generated path cross the non-fly bilidings, and can not be avoid by using landmark,
     * try to find an angel that makes drone closer to destination and not cross non-flay zone
     * @param droneCurrentPosition the current position of the drone
     * @param destination the destination of the drone
     * @return angle that allows drone to avoid flay to non-fly zone.
     */
    private int getAvoidingAngle(LongLat droneCurrentPosition,LongLat destination){
        double minDistance = 1000000000;
        int bestAngle = 0;
        for (int angle = 0; angle < 360; angle += 10) {
            // Check possible drone position
            var droneNextPosition = droneCurrentPosition.nextPosition(angle);
            double distance = droneNextPosition.distanceTo(destination);
            //create a path that can be used in method checkMeetNonFly
            ArrayList<LongLat> unitPath = new ArrayList<>();
            unitPath.add(droneCurrentPosition);
            unitPath.add(droneNextPosition);
            if (!checkMeetNonFly(unitPath) & droneNextPosition.isConfined()) {
                // check for the min distance, chose the angle lead to a position closest to target
                if (distance < minDistance) {
                    minDistance = distance;
                    bestAngle = angle;
                }
            }
        }
        return bestAngle;
    }

    /**
     * generate the direct flight path between 2 location.
     * @param start the start position
     * @param destination the destination location
     * @return list of longlat objects indicating the path of flight
     */
    private ArrayList<LongLat> generateDirectPath(LongLat start, LongLat destination){
        ArrayList<LongLat> path = new ArrayList<>();
        this.angles=new ArrayList<Integer>();
        LongLat currentPosition = start;
        path.add(start);
        while (!currentPosition.closeTo(destination)){
            // find angle of direct linked line between 2 position
            int angle =currentPosition.calculateAngle(destination);
            currentPosition=currentPosition.nextPosition(angle);
            this.angles.add(angle);
            path.add(currentPosition);
        }
        return path;
    }


    /**
     * check if the given path cross the non-fly-zone or not.
     * @param path a drone flight path.
     * @return true if the path cross the non-fly-zone.
     */
    private boolean checkMeetNonFly(ArrayList<LongLat> path) {
        boolean crossNoFlyZone = false;
        for (int i = 0; i < path.size()-1; i++) {
            //create line that indicating unit move i.e. move 0.00015 degree in path list
            Line2D linePath = new Line2D.Double(path.get(i).latitude,path.get(i).longitude,path.get(i+1).latitude,path.get(i+1).longitude);
            for (NonFlyZone n : this.nonFlyZone) {
                List<Point> coordinates = n.getCoordinates();
                for (int j = 0; j < coordinates.size() - 1; j++) {
                    int k = (j + 1) % coordinates.size();
                    // create lines indicating border of the non-fly zone
                    Line2D border = new Line2D.Double(coordinates.get(j).latitude(),
                            coordinates.get(j).longitude(),
                            coordinates.get(k).latitude(),
                            coordinates.get(k).longitude());
                    //check if unit move line cross the border.
                    if (linePath.intersectsLine(border)) {
                        crossNoFlyZone = true;
                        return crossNoFlyZone;
                    }
                }
            }
        }
        return crossNoFlyZone;
    }

    /**
     * with the provided list of Longlat, drone perform moves though the path and record the move in database.
     * @param path the path that the drone will move.
     */
    private void moveAndRecordPath(ArrayList<LongLat> path,ArrayList<Integer>pathAngles){
        // template copypath used to store the changed path.
        ArrayList<LongLat> copyPath =path;
        // go though the path, find each unit move,
        for (int i = 0; i < path.size() - 1; i++) {
            if(this.batteryLife>0) {
                ArrayList<LongLat> unitPath = new ArrayList<>();
                unitPath.add(path.get(i));
                unitPath.add(path.get(i + 1));
                // check if current unit move cross the non-fly zone.
                if(checkMeetNonFly(unitPath)| !path.get(i + 1).isConfined()){
                    // if so, re-calculate direction angle to avoid fly though non-flt zone.
                    int newAngle = getAvoidingAngle(path.get(i),path.get(i+1));
                    // with new direction angle, update list of path angle and path copypath, so right path stored in allPath and database.
                    pathAngles.set(i,newAngle);
                    LongLat newNextPos = path.get(i).nextPosition(newAngle);
                    copyPath.set(i+1,newNextPos);
                    if (allPath.size()==0){
                        allPath.add((copyPath.get(0)));
                    }
                    allPath.add(copyPath.get(i+1));
                    db.insertFlightPath(this.nextOrder.getOrderNo(), path.get(i).longitude, path.get(i).latitude, pathAngles.get(i), newNextPos.longitude, newNextPos.latitude);
                }else {
                    if (allPath.size()==0){
                        allPath.add((path.get(0)));
                    }
                    // if unit path not cross the non-fly zone, just record it.
                    allPath.add(path.get(i+1));
                    db.insertFlightPath(this.nextOrder.getOrderNo(), path.get(i).longitude, path.get(i).latitude, pathAngles.get(i), path.get(i + 1).longitude, path.get(i + 1).latitude);
                }
                //perform a drone move, reduce battery life.
                unitMove(pathAngles.get(i));
            }else break;
        }
    }

    /**
     * use to select path, and use method moveAndRecordPath to perform movement.
     * @param currentposition start position
     * @param destination destination of the path
     */
    private ArrayList<LongLat> generateLandmarkPath(LongLat currentposition,LongLat destination) {
        LongLat nextPosition = null;
        nextPosition = destination;
        // generate direct path between drone current position and the destination
        ArrayList<LongLat> path = this.generateDirectPath(currentposition, nextPosition);
        LongLat closestLandmark =null;
        double distance1 = 0;
        double distance2 = 0;
        // if the direct path cross the non-fly zone or not.
        if (this.checkMeetNonFly(path)) {
            //select landmarks to try to avoid fly across the non-fly zone
            // loop though all landmarks
            for (int i=0; i<landmarks.size();i++){
                LongLat landMark = new LongLat(landmarks.get(i).getLocation().longitude(), landmarks.get(i).getLocation().latitude());
                nextPosition = landMark;
                ArrayList<LongLat> pathToLandmark= this.generateDirectPath(currentposition,nextPosition);
                ArrayList<LongLat> pathToDest = this.generateDirectPath(nextPosition,destination);
                //if current landmark could use to avoid non-fly zone, check if it is the one closest to current position.
                // Thus, find the landmark that is closest to current and could avoid non-fly zone.
                if(!checkMeetNonFly(pathToLandmark)& !checkMeetNonFly(pathToDest)){
                    if (closestLandmark == null){
                        closestLandmark=landMark;
                    }else {
                        distance1=closestLandmark.distanceTo(currentposition);
                        distance2=landMark.distanceTo(currentposition);
                        if (distance1>distance2){
                            closestLandmark=landMark;
                        }
                    }
                }
            }
            //if all land marks cannot avoid flay though non-fly zone, just use the last landmark. else, choose closest landmark
            if(closestLandmark!=null){
                nextPosition=closestLandmark;
            }

            //fly to chosen landmark
            path = this.generateDirectPath(currentposition,nextPosition);
            ArrayList<Integer> tempAngles = this.angles;
            ArrayList<LongLat>pathToDest= this.generateDirectPath(path.get(path.size()-1),destination);
            for (int i=1;i<pathToDest.size();i++){
                path.add(pathToDest.get(i));
                tempAngles.add(this.angles.get(i-1));
            }
            this.angles=tempAngles;
            return path;
            //moveAndRecordPath(path);
        }
        //fly from current position to destination
        path= this.generateDirectPath(currentposition,destination);
        //moveAndRecordPath(path);
        return path;
    }

    /**
     * loop to start the delivery, stop until all order is delivered or drone out of battery.
     */
    public void start(){
        ArrayList<LongLat> path = null;
        ArrayList<LongLat> pathBack = null;
        //loop though all orders
        while(this.remainOrders.size()>0 & this.batteryLife>0){
            this.getNextMenuAndOrder();
            if(this.targetShops.size()>2){
                System.exit(1);
            }
            for (ShopLonglat s:this.targetShops){
                // check if there is enough battery to go back after drone move to destination shop.
                pathBack= generateLandmarkPath(s.location,this.startpos);
                path = generateLandmarkPath(this.position,s.location);
                int tempBattery = this.batteryLife;
                // if enough battery, perform the fly
                if (tempBattery-path.size()-1>pathBack.size()){
                    moveAndRecordPath(path,this.angles);
                    this.collectFood();
                    // for checking how many hovering amd by drone
                    this.hovercount=this.hovercount+1;
                }else{
                    // if not enough, fly back from current position
                    pathBack= generateLandmarkPath(this.position,this.startpos);
                    moveAndRecordPath(pathBack,this.angles);
                    break;
                }
            }
            // check if there is enough battery to go back after drone move to destination.
            pathBack= generateLandmarkPath(this.nextOrder.getDeliverTo(),this.startpos);
            path = generateLandmarkPath(this.position,this.nextOrder.getDeliverTo());
            int tempBattery = this.batteryLife;
            if (tempBattery-path.size()-1>pathBack.size()){
                moveAndRecordPath(path,this.angles);
                this.deliverFood();
                this.hovercount=this.hovercount+1;
                db.insertDeliveries(this.nextOrder.getOrderNo(), this.nextOrder.getDeliverToW3W(), this.nextOrder.getPrice());
            }else{
                pathBack= generateLandmarkPath(this.position,this.startpos);
                moveAndRecordPath(pathBack,this.angles);
                break;
            }
        }
        if(this.remainOrders.size()==0){
            // all order delivered, back to appleton tower
            path = generateLandmarkPath(this.position,this.startpos);
            moveAndRecordPath(path,this.angles);
        }
    }
    /**
     * drones collect the food, by hovering once.
     */
    private void collectFood(){
        unitMove(-999);
    }

    /**
     * drone deliever the food, by hovering once.
     */
    private void deliverFood(){
        unitMove(-999);
    }

    /**
     * drone movement, including fly and hovering.
     * @param angle direction of movement.
     */
    private void unitMove(int angle){
        try {
            // update the position after move
            this.position = this.position.nextPosition(angle);
            // update the batterylife by -1
            this.batteryLife = this.batteryLife - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * get all of the path of the day
     * @return all of the path of the day
     */
    public ArrayList<LongLat> getAllPath() {
        return allPath;
    }
}
