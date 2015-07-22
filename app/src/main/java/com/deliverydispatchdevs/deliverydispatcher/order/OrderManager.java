package com.deliverydispatchdevs.deliverydispatcher.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class OrderManager
{
    private static Collection<DeliveryOrder> mUndispatchedOrders = new ArrayList<>();
    private static Collection<DeliveryOrder> mDispatchedOrders = new ArrayList<>();
    private static Collection<OrderGroup> mOrderGroups = new ArrayList<>();

    public static Collection<DeliveryOrder> getDispatchedOrders(){return mDispatchedOrders;}

    public static Collection<DeliveryOrder> getUndispatchedOrders(){return mUndispatchedOrders;}

    public static Collection<OrderGroup> getOrderGroups(){return mOrderGroups;}

    public static void placeOrder(DeliveryOrder order)
    {
        mUndispatchedOrders.add(order);
    }

     public static boolean dispatchOrder(DeliveryOrder order)
    {
        mDispatchedOrders.add(order);
        return mUndispatchedOrders.remove(order);
    }

    public static OrderGroup createGroup()
    {
        return new OrderGroup();
    }

    public static boolean dispatchGroup(OrderGroup group)
    {
        return mOrderGroups.remove(group);
    }

    public static void clearAll()
    {
        mDispatchedOrders.clear();
        mUndispatchedOrders.clear();
        mOrderGroups.clear();
    }

}


