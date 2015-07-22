package com.deliverydispatchdevs.deliverydispatcher.order;

import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Created by Saad on 7/16/2015.
 */
public class Time
{
   private int mHours, mMinutes, mSeconds;

    public void setHours(int hours){mHours = hours; }

    public void setMinutes(int minutes) {mMinutes = minutes; }

    public void setSeconds(int seconds) {mSeconds = seconds; }

    public int getHours() {return mHours;}

    public int getMinutes() {return mMinutes;}

    public int getSeconds() {return mSeconds;}

    public Time(int hour, int min, int sec)
    {
        mHours = hour;
        mMinutes=min;
        mSeconds=sec;
    }

    public Time(int hour, int min)
    {
        mHours = hour; mMinutes = min;
    }

    public Time(int hour)
    {
        mHours = hour;
    }

    public boolean equals(Object other)
    {
        if (getClass() != other.getClass())
            return false;
        Time time = (Time) other;
        return time.getHours() == mHours
                && time.getMinutes() == mMinutes
                && time.getSeconds() == mSeconds;
    }

    public int compareTo(Object other)
    {
        Time time = (Time) other;
        if(time.getHours()==mHours)
            if(time.getMinutes()==mMinutes)
                if(time.getSeconds()<mSeconds)
                    return 1;
                else if(time.getSeconds()>mSeconds)
                    return -1;
                else return 0;
            else if(time.getMinutes()<mMinutes)
                return 1;
            else if(time.getMinutes()>mMinutes)
                return -1;
            else return 0;
        else if(time.getHours()<mHours)
            return 1;
        else if(time.getHours()>mHours)
            return -1;
        else return 0;

    }

    public String toString()
    {
        String s = "";
        if(mHours<10){ s+= "0";}
        s+=mHours; s+=":";
        if(mMinutes<10) {s+="0";}
        s+= mMinutes; s+= ":";
        if(mSeconds<10) {s+=0;}
        s+= mSeconds;
        if(mHours<12) s+=" AM"; else s+=" PM";
        return s;
    }
}
