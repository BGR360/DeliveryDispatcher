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

    /**
     * Dispatches the given order by moving it from the undispatched list
     * to the dispatched list. If it was not in the undispatched list,
     * does not add it to the dispatched list.
     *
     * @param order The DeliveryOrder we want to dispatch
     * @return Returns true if the order was contained in mUndispatchedOrders, false if not.
     */
     public static boolean dispatchOrder(DeliveryOrder order)
     {
         // If the order was in mUndispatchedOrders, move it to mDispatchedOrders
         // Otherwise, we have to return false
         if (mUndispatchedOrders.remove(order)) {
             mDispatchedOrders.add(order);
         } else {
             return false;
         }
         return true;
     }

     @SuppressWarnings("ManualArrayToCollectionCopy")
     public static OrderGroup createGroup(DeliveryOrder[] orders)
     {
         OrderGroup newGroup = new OrderGroup();
         for(DeliveryOrder order : orders)
         {
             newGroup.add(order);
         }
         return newGroup;
     }

    public static boolean dispatchGroup(OrderGroup group)
    {
        boolean ordersDispatched = true;

        for(DeliveryOrder order : group)
        {
            ordersDispatched &= dispatchOrder(order);
        }

        return ordersDispatched;
    }

    public static void clearAll()
    {
        mDispatchedOrders.clear();
        mUndispatchedOrders.clear();
        mOrderGroups.clear();
    }

}


