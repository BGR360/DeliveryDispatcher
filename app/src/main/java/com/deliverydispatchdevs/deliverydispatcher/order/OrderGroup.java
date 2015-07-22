package com.deliverydispatchdevs.deliverydispatcher.order;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Saad on 7/16/2015.
 */
public class OrderGroup
{
    private Collection<DeliveryOrder> mOrders = new ArrayList<DeliveryOrder>();
    private Color mColor;

    public Color getColor()
    {
        return mColor;
    }
    public Collection<DeliveryOrder> getOrders()
    {
        return mOrders;
    }

    public void addOrder(DeliveryOrder order)
    {
        mOrders.add(order);
    }


    public boolean removeOrder(DeliveryOrder order)
    {
            return mOrders.remove(order);
    }

    public void clear()
    {
        mOrders.clear();
    }

    public void setColor(Color color)
    {
        mColor = color;
    }

    public int getNumOrders()
    {
        return mOrders.size();
    }

    public boolean equals(Object other)
    {
        if (getClass() != other.getClass())
            return false;
        OrderGroup newGroup = (OrderGroup) other;
        if(newGroup.getColor()!=mColor) return false;
        if(newGroup.getNumOrders()!=mOrders.size()) return false;
        for(int count= 0; count<mOrders.size();count++)
        {
            if(!(((ArrayList)mOrders).get(count).equals(( (ArrayList)newGroup.getOrders() ).get(count))))
            {
                return false;
            }
        } return true;
    }

    public int compareTo(Object other)
    {
        OrderGroup group = (OrderGroup) other;
        return this.getEarliestDueTime().compareTo(group.getEarliestDueTime());
    }

    public String toString()
    {
        String s = "";
        for(DeliveryOrder order: mOrders)
        {
            s+= order.getOrderNumber() + ",";
        }
        s+= mColor.toString();
        return s;
    }

    public Time getEarliestDueTime()
    {
        Time earliestDue = ( (ArrayList<DeliveryOrder>)mOrders ).get(0).getPromisedTime();
        for(int count = 1; count<mOrders.size();count++)
        {
            if( ((ArrayList<DeliveryOrder>)mOrders).get(count).getPromisedTime().compareTo(earliestDue) == -1 )
            {
                earliestDue = ((ArrayList<DeliveryOrder>)mOrders).get(count).getPromisedTime();
            }
        } return earliestDue;
    }
}


