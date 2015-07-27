package com.deliverydispatchdevs.deliverydispatcher.order;


public class Address
{
    public Address(String addresses)
    {
        mAddress = addresses;
    }

    private String mAddress;
    public void setAddress(String address) { mAddress = address; }
    public String getAddress() {return mAddress;}

    public String toString(){ return mAddress;}
    public boolean equals(Object other)
    {
        if (getClass() != other.getClass())
            return false;
        Address address = (Address) other;
        return address.toString().equals(mAddress);
    }
}

