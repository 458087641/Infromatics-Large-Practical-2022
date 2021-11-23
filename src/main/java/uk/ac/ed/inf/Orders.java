package uk.ac.ed.inf;

public class Orders {
    public String orderID;
    public String deliveryDate;
    public String customer;
    public String receiver;
    public LongLat destination;
    public String item;
    public int price;

    public Orders(String orderID,String deliveryDate,String customer,String receiver,LongLat destination,String item) {
        this.orderID = orderID;
        this.deliveryDate = deliveryDate;
        this.customer = customer;
        this.receiver = receiver;
        this.destination = destination;
        this.item = item;
    }

    public String getOrderNo() {
        return orderID;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDeliverTo() {
        return receiver;
    }

    public String getItem() {
        return item;
    }

    public LongLat getDestination() {return this.destination;}
}
