package com.deliverydispatchdevs.deliverydispatcher.order;


public class DeliveryOrder

{
    private Address mDestination;
    private Time mPlacedTime;
    private Time mReadyTime;
    private Time mPromisedTime;
    private int mOrderNumber;
    private String mCustomerName;

    public DeliveryOrder()
    {
        this(null, null, null, null, -1);
    }

    public DeliveryOrder(Address destination, Time placedTime, Time readyTime, Time promisedTime, int orderNumber)
    {
        mDestination = destination;
        mPlacedTime = placedTime;
        mReadyTime = readyTime;
        mPromisedTime = promisedTime;
        mOrderNumber = orderNumber;
    }

    public  DeliveryOrder(int orderNum, Address addresses, Time dueTime)
    {
        mOrderNumber = orderNum;
        mDestination = addresses;
        mPromisedTime = dueTime;
    }

    public Address getDestination() {return mDestination; }

    public Time getPlacedTime() {return mPlacedTime;}

    public Time getReadyTime() {return mReadyTime;}

    public int getOrderNumber() { return mOrderNumber;}

    public String getCustomerName() { return mCustomerName;}

    public Time getPromisedTime() { return mPromisedTime;}

    public void setDestination(Address destination) {mDestination = destination;}

    public void setPlacedTime(Time placedTime) {mPlacedTime = placedTime;}

    public void setReadyTime(Time readyTime) {mReadyTime = readyTime;}

    public void setPromisedTime(Time promisedTime) {mPromisedTime = promisedTime;}

    public void setOrderNumber(int orderNumber) {mOrderNumber = orderNumber;}

    public void setCustomerName(String customerName) {mCustomerName = customerName;}

     public boolean equals(Object other)
    {
        if (getClass() != other.getClass())
            return false;
        DeliveryOrder deliveryOrder = (DeliveryOrder)other;
        return deliveryOrder.getDestination().equals(mDestination)
                && deliveryOrder.getCustomerName().equals(mCustomerName)
                && deliveryOrder.getOrderNumber()== mOrderNumber
                && deliveryOrder.getPlacedTime().equals(mPlacedTime)
                && deliveryOrder.getPromisedTime().equals(mPromisedTime)
                && deliveryOrder.getReadyTime().equals(mReadyTime);
    }

    public int compareTo(Object other)
    {
        DeliveryOrder order = (DeliveryOrder) other;
        if (order.getPromisedTime().equals(mPromisedTime))
            if (order.getOrderNumber() < mOrderNumber)
                return -1;
            else if (order.getOrderNumber() > mOrderNumber)
                return 1;
            else return 0;
        else return mPromisedTime.compareTo(order.getPromisedTime());
    }

    public String toString()
    {
        return "Order Number: " + mOrderNumber + ", Address: " + mDestination.toString() + ", Due Time: " + mPromisedTime.toString();
    }

}
