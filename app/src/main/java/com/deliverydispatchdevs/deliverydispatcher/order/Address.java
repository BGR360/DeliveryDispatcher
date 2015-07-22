package com.deliverydispatchdevs.deliverydispatcher.order;

/**
 * Created by Saad on 7/16/2015.
 */
public class Address
{
    private String mAddress;
    public void setAddress(String address) { mAddress = address; }
    public String getAddress() {return mAddress;}

    public String toString(){ return mAddress;}
    public boolean equals(Object other)
    {
        if (getClass() != other.getClass())
            return false;
        Address address = (Address) other;
        return address.toString().equals(mAddress.toString());
    }
}

