package uk.ac.ed.inf;

import java.util.ArrayList;
/**
 * structure for orders get from database
 */
public class Orders {
    private String orderNo;
    private String deliveryDate;
    private String customer;
    private String deliverToW3W;
    private LongLat deliverTo;
    private ArrayList<String> item;
    private int price;

    /**
     * constructor
     * @param orderNo order number
     * @param deliveryDate delivery date
     * @param customer customer of the order
     * @param deliverToW3W the W3W location of the order's destination
     * @param item items of the order
     */
    public Orders(String orderNo,String deliveryDate,String customer,String deliverToW3W,ArrayList<String> item) {
        this.orderNo = orderNo;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.deliverToW3W = deliverToW3W;
        this.item = item;
    }

    /**
     * get the W3W location String of order's receiver position
     * @return W3W location String
     */
    public String getDeliverToW3W() { return deliverToW3W; }

    /**
     * get the total cost of the order
     * @return total cost of the order
     */
    public int getPrice() { return price; }

    /**
     * get the order number
     * @return order number
     */
    public String getOrderNo() {
        return this.orderNo;
    }

    /**
     * get the delivery date of the order
     * @return delivery date of the order
     */
    public String getDeliveryDate() {
        return this.deliveryDate;
    }

    /**
     * get the customer of the order
     * @return customer of the order
     */
    public String getCustomer() {
        return this.customer;
    }

    /**
     * get the receiver's coordinate
     * @return the receiver's coordinate
     */
    public LongLat getDeliverTo() {
        return this.deliverTo;
    }

    /**
     * get all items in the oreder
     * @return all items in the oreder
     */
    public ArrayList<String> getItem() {
        return this.item;
    }

    /**
     * set the order's total price
     * @param price order's total price
     */
    public void setPrice(int price) { this.price = price; }

    /**
     * set the receiver's location in LongLat
     * @param longLat the receiver's location
     */
    public void setDeliverTo(LongLat longLat){ this.deliverTo=longLat; }
}
