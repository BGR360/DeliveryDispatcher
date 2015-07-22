package com.deliverydispatchdevs.deliverydispatcher.order;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Saad on 7/16/2015.
 */
public class OrderGroup extends ArrayList<DeliveryOrder>
{
    private Color mColor;

    public Color getColor()
    {
        return mColor;
    }

    public void setColor(Color color)
    {
        mColor = color;
    }

    public int getNumOrders()
    {
        return this.size();
    }

    public boolean equals(Object other)
    {
        if (getClass() != other.getClass())
            return false;
        OrderGroup newGroup = (OrderGroup) other;
        if(newGroup.getColor()!=mColor) return false;
        if(newGroup.getNumOrders()!=this.size()) return false;
        for(int count= 0; count<this.size();count++)
        {
            if(!(this.get(count).equals( newGroup.get(count))))
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
        for(DeliveryOrder order: this)
        {
            s+= order.getOrderNumber() + ",";
        }
        s+= mColor.toString();
        return s;
    }

    public Time getEarliestDueTime()
    {
        Time earliestDue = ( (ArrayList<DeliveryOrder>)this ).get(0).getPromisedTime();
        for(int count = 1; count<this.size();count++)
        {
            if( ((ArrayList<DeliveryOrder>)this).get(count).getPromisedTime().compareTo(earliestDue) == -1 )
            {
                earliestDue = ((ArrayList<DeliveryOrder>)this).get(count).getPromisedTime();
            }
        } return earliestDue;
    }
}


