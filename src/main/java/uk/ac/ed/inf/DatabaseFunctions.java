package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

/**
 * class contains methods that interact with database, for get, insert data from database
 * also create new tables in the database.
 */
public class DatabaseFunctions {
    private String server;
    private String port;
    private String jdbcString;

    /**
     * constructor
     * @param port port of connection
     * @param server localhost
     */
    public DatabaseFunctions(String port, String server){
        this.server=server;
        this.port=port;
        this.jdbcString="jdbc:derby://" +this.server+":"+ this.port+"/derbyDB";
    }

    /**
     * check if it is able to connect to database server
     * @return true if successfully connected to database
     */
    public boolean connectToDB(){
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            DatabaseMetaData metaData = conn.getMetaData();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * check if table Deliveries already exist, if it exists, delete it and create a new empty Deliveries. if not, create new Deliveries.
     * @return true if delete and recreate success;
     */
    public boolean checkDeliveries(){
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            Statement statement = conn.createStatement();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "DELIVERIES", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table DELIVERIES");
            }
            // create new table that is empty.
            statement.executeUpdate("create table deliveries(orderNo char(8),deliveredTo varchar(19),costInPence int)");
            statement.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    /**
     * insert a instance into table Deliveries
     * @param orderNo delivered order number
     * @param deliveredTo delivered order's destination.
     * @param pence delivered order's value
     * @return true if insertion sucess.
     */
    public boolean insertDeliveries(String orderNo, String deliveredTo, int pence){
        final String sql = "insert into DELIVERIES values((?),(?),(?))";
        PreparedStatement psDelivery = null;

        try{
            Connection conn = DriverManager.getConnection(jdbcString);
            psDelivery = conn.prepareStatement(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            // set parameters to the sql query.
            psDelivery.setString(1,orderNo);
            psDelivery.setString(2,deliveredTo);
            psDelivery.setInt(3,pence);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            psDelivery.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    /**
     * check if table Flightpath already exist, if it exists, delete it and create a new empty Flightpath. if not, create new Flightpath.
     * @return true if delete and recreate success;
     */
    public boolean checkFlightpath(){
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            DatabaseMetaData databaseMetadata = conn.getMetaData();
            Statement statement = conn.createStatement();
            ResultSet resultSet =
                    databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            // If the resultSet is not empty then the table exists, so we can drop it
            if (resultSet.next()) {
                statement.execute("drop table FLIGHTPATH");
            }
            // create new table that is empty.
            statement.executeUpdate("create table flightpath(orderNo char(8),fromLongitude double,fromLatitude double,angle integer,toLongitude double,toLatitude double)");
            statement.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    /**
     * insert new instance into table Flightpath
     * @param orderNo order number that is currently delivering
     * @param fromLong original position longitude
     * @param fromLat original position latitude
     * @param angle flying angle from original position
     * @param toLong longitude after move from original position
     * @param toLat latitude after move from original position.
     * @return true if insertion success.
     */
    public boolean insertFlightPath(String orderNo,double fromLong,double fromLat,int angle,double toLong,double toLat) {
        final String sql = "insert into FLIGHTPATH values((?),(?),(?),(?),(?),(?))";
        PreparedStatement psPath = null;
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            psPath = conn.prepareStatement(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            // set parameters to the sql query.
            psPath.setString(1, orderNo);
            psPath.setDouble(2, fromLong);
            psPath.setDouble(3, fromLat);
            psPath.setInt(4, angle);
            psPath.setDouble(5, toLong);
            psPath.setDouble(6, toLat);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        try {
            psPath.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }
    /**
     * get orders placed on a given date
     * @param date date to find related orders form database
     * @return orders placed on that date
     */
    public ArrayList<Orders> getOrderByDate(String date){
        // get orders by date
        final String sqlOrder = "select * from ORDERS where ORDERS.deliveryDate = (?)";
        //get order items by order number
        final String sqlDetails = "select item from ORDERDETAILS where ORDERDETAILS.orderNo=(?)";
        PreparedStatement queryOrder = null;
        PreparedStatement queryDetails = null;
        try {
            Connection conn = DriverManager.getConnection(jdbcString);
            queryOrder = conn.prepareStatement(sqlOrder);
            queryDetails =conn.prepareStatement(sqlDetails);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        try {
            queryOrder.setString(1, date);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

        ArrayList<Orders> orders = new ArrayList<>();
        try {
            // get order information of given date.
            ResultSet rsOrder = queryOrder.executeQuery();
            // go though result set to get order information one by one;
            while (rsOrder.next()) {
                String orderNo = rsOrder.getString(1);
                String deliveryDate = rsOrder.getString(2);
                String customer = rsOrder.getString(3);
                String deliverToW3W = rsOrder.getString(4);
                //get items related to current order number
                queryDetails.setString(1, orderNo);
                ResultSet rsDetails = queryDetails.executeQuery();
                ArrayList<String> items = new ArrayList<>();
                // add all items related to current order to list.
                while (rsDetails.next()){
                    items.add(rsDetails.getString(1));
                }
                // create Order class instance and add to list.
                orders.add(new Orders(orderNo,deliveryDate,customer,deliverToW3W,items));
            }

        }
        catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return orders;
    }

}
