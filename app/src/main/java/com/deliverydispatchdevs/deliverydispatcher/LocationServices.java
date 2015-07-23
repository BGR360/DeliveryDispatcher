package com.deliverydispatchdevs.deliverydispatcher;

/**
 * Created by BGR on 7/15/2015.
 *
 * This class handles the information regarding location services,
 * such as the LocationManager. It can provide information about
 * the device's last known location or asynchronously search
 * for the device's location (for a specified length of time in
 * milliseconds) if no suitable last-known location exists.
 *
 * NOTE: MUST CALL initialize(Context) FROM MAIN ACTIVITY
 * BEFORE CLASS BECOMES FUNCTIONAL
 *
 * All public methods are static.
 */

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LocationServices
{
    private static final String LOG_TAG = "LocationServices";

    // By default, we should spend 1 minute, and no more, searching for a Location
    private static final long DEFAULT_SEARCH_TIME_MILLIS = 1000 * 60;


    //---------MEMBER VARIABLES---------//

    private static Context mContext;
    private static LocationManager mLocationManager;
    private static boolean mIsSearching = false;

    // This class we will attach to mLocationManager to receive location updates
    private static MyLocationChangedListener mLocationChangedListener;

    // This interface handles the callback that is called when location is found or not found
    // Assigned and used by the caller of beginFindLocation()
    private static ArrayList<OnLocationFoundListener> mLocationFoundListeners = new ArrayList<>();

    // The Timer will handle the timeout
    // Invariant: Timer will be null when isSearching is false,
    // and not null when isSearching is true
    private static Timer mStopTimer = null;

    /**
     * One big issue we must address is synchronization. A locationChanged() update could
     * occur at any time, even in the middle of executing beginFindLocation method.
     * So we must ensure that, if an update comes while we are in the middle of one
     * of our methods, we do not call endFindLocation() until we finish those methods.
     * We accomplish this with the use of a custom Lock class. At the beginning of
     * important methods, we call lock(), and at the end we call unlock(). If
     * we received a location update between the call to lock() and the call to
     * unlock(), the call to unlock() will call endFindLocation().
     */
    private static final Lock mLock = new Lock();


    //---------PUBLIC METHODS---------//

    /**
     * MUST CALL THIS METHOD BEFORE ANY OTHER METHODS
     * CAN BE USED PROPERLY.
     * Recommended to call this method in Activity.onCreate()
     *
     * @return True if we succeeded in initializing, false if not
     */
    public static boolean initialize(Context context)
    {
        Log.d(LOG_TAG, "Initializing Location Services...");
        mContext = context;
        boolean success = retrieveLocationServices();
        if(success)
        {
            // Initialize the MyLocationChangedListener, but don't attach it yet
            mLocationChangedListener = new MyLocationChangedListener();
        }
        else
        {
            Log.w(LOG_TAG, "Location Services are unavailable.");
        }
        return success;
    }

    /**
     * Call this method when the resources provided and encapsulated by this class
     * are no longer immediately needed.
     */
    public static synchronized void shutdown()
    {
        Log.d(LOG_TAG, "Shutting down Location Services...");

        // If we are in the middle of a search, we need to cancel and detach ourselves
        if(mIsSearching)
        {
            //noinspection StatementWithEmptyBody
            while(mLock.isLocked());
            endFindLocation(null);
        }

        mContext = null;
        mLocationManager = null;
        mLocationChangedListener = null;
        mLocationFoundListeners.clear();
        mLocationFoundListeners = null;
        mStopTimer = null;
    }

    /**
     * Attempts to get a LocationManager from mContext (can result in null).
     * Can be called repeatedly if it failed the first time(s).
     *
     * @return True if we received a LocationManager, false if not
     */
    public static boolean retrieveLocationServices()
    {
        if(mContext != null)
        {
            mLocationManager = (LocationManager) mContext.getSystemService(
                    Context.LOCATION_SERVICE);
        }
        return isLocationServicesAvailable();
    }

    /**
     * Tells whether or not Location Services are available.
     *
     * @return True if mLocationManager is non-null, false if it is null
     */
    public static boolean isLocationServicesAvailable()
    {
        return mLocationManager != null;
    }

    /**
     * Searches all the available location providers to find the
     * most accurate lastKnownLocation. Recommended to use
     * this method before calling beginFindLocation() to
     * avoid an unnecessary search.
     *
     * @return The most accurate lastKnownLocation that we found, or null
     * if we couldn't find one, or null if we have a null LocationManager.
     */
    public static Location getLastKnownLocation()
    {
        Log.d(LOG_TAG, "Getting lastKnownLocation...");
        Location bestLastKnownLocation = null;

        // LocationManager must be valid
        if(isLocationServicesAvailable())
        {
            // Search all available providers for the most accurate lastKnownLocation
            List<String> providers = mLocationManager.getProviders(true);

            if(providers != null && providers.size() > 0)
            {
                for(String provider : providers)
                {
                    // Get the last known location from the current location provider
                    Location location = mLocationManager.getLastKnownLocation(provider);

                    // getLastKnownLocation() can return null
                    if(location == null)
                    {
                        continue;
                    }

                    // Check to see if location is a better fit than bestLastKnownLocation
                    if(bestLastKnownLocation == null ||
                            location.getAccuracy() < bestLastKnownLocation.getAccuracy())
                    {
                        bestLastKnownLocation = location;
                    }
                }

                if(bestLastKnownLocation == null)
                {
                    Log.w(LOG_TAG, "Unable to find lastKnownLocation: " +
                            "No location provider knows what it is.");
                }
                else
                {
                    Log.d(LOG_TAG, "Found the best lastKnownLocation: "
                            + bestLastKnownLocation.toString());
                }
            }
            else
            {
                // No available location providers
                Log.w(LOG_TAG, "Unable to find lastKnownLocation: No location provider available.");
            }
        }
        else
        {
            // Invalid LocationManager
            Log.w(LOG_TAG, "Unable to find lastKnownLocation: Location Services are unavailable.");
        }

        return bestLastKnownLocation;
    }

    /**
     * Begins the search to find the device's location. When it finishes or times out,
     * it will invoke onLocationFound(Location) on all OnLocationFoundListeners
     * in mLocationFoundListeners. If a listener cancels its request for a Location,
     * but there are still others waiting, the search will continue.
     *
     * @param timeoutMillis The amount of time, in milliseconds,
     *                      that we should search for a location
     * @return True if we were able to include listener in a new or existing search, or false
     * if we were unable to begin searching or if listener was already listening
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean beginFindLocation(long timeoutMillis, OnLocationFoundListener listener)
    {
        mLock.lock();

        boolean listenerSatisfied = false;

        Log.d(LOG_TAG, "Somebody wants to know our location...");

        // If we are already in the middle of a search, simply add listener to our list.
        // It will later be notified once our ongoing search is complete.
        if(mIsSearching)
        {
            if(addListener(listener))
            {
                Log.d(LOG_TAG, "Added listener to the list.");
                listenerSatisfied = true;
            }
            else
            {
                Log.d(LOG_TAG, "Listener was already listening!");
            }
        }
        else
        {
            // We were not searching, so start a new search.
            Log.d(LOG_TAG, "Beginning find location...");
            if(isLocationServicesAvailable())
            {
                // As long as Location Services are available, we can check
                // to see which location providers are available for use.
                boolean networkIsAvailable = mLocationManager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER);
                boolean passiveIsAvailable = mLocationManager.isProviderEnabled(
                        LocationManager.PASSIVE_PROVIDER);
                boolean gpsIsAvailable = mLocationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER);

                String available = networkIsAvailable ? "available" : "not available";
                Log.d(LOG_TAG, "Network provider is " + available + ".");
                available = passiveIsAvailable ? "available" : "not available";
                Log.d(LOG_TAG, "Passive provider is " + available + ".");
                available = gpsIsAvailable ? "available" : "not available";
                Log.d(LOG_TAG, "GPS provider is " + available + ".");

                // We can begin the search if any provider is available
                if(networkIsAvailable || passiveIsAvailable || gpsIsAvailable)
                {
                    // Add listener as the first listener in our list
                    addListener(listener);

                    // Begin the search by subscribing to location updates

                    // LocationManager.requestLocationUpdates() takes three parameters:
                    // 1: the provider
                    // 2: how often you want to receive updates (millis)
                    // 3: minimum required change in distance to require an update (meters)
                    // 4: a LocationListener

                    Log.d(LOG_TAG, "Attaching to location updates...");
                    // Attach to location updates from the best provider
                    // Prefer network over passive and passive over gps
                    if(networkIsAvailable)
                    {
                        Log.d(LOG_TAG, "Using Network provider.");
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, 0, 0, mLocationChangedListener);
                    }
                    else if (passiveIsAvailable)
                    {
                        Log.d(LOG_TAG, "Using Passive provider.");
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, 0, 0, mLocationChangedListener);
                    }
                    else if (gpsIsAvailable)
                    {
                        Log.d(LOG_TAG, "Using GPS provider.");
                        mLocationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER, 0, 0, mLocationChangedListener);
                    }

                    // Start the timeout timer
                    mStopTimer = new Timer();
                    mStopTimer.schedule(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            // Stop searching after timeoutMillis, regardless of
                            // whether or not we have found the location
                            Log.d(LOG_TAG, "Search timed out!");
                            mLock.requestEndFindLocation(null);
                        }
                    }, timeoutMillis);

                    mIsSearching = true;
                    listenerSatisfied = true;
                }
                else
                {
                    // No location provider available
                    Log.w(LOG_TAG, "Unable to begin finding location: " +
                            "No location provider available.");
                }
            }
            else
            {
                // Location Services unavailable
                Log.w(LOG_TAG, "Unable to begin finding location: " +
                        "Location Services are unavailable.");
            }
        }

        mLock.unlock();
        return listenerSatisfied;
    }

    /**
     * Overloaded from beginFindLocation(long, OnLocationFoundListener)
     */
    public static boolean beginFindLocation(OnLocationFoundListener listener)
    {
        return beginFindLocation(DEFAULT_SEARCH_TIME_MILLIS, listener);
    }

    /**
     * This method can be called from outside the class to cancel
     * the search for a specific OnLocationFoundListener.
     * If a listener cancels its request for a Location,
     * but there are still others waiting, the search will continue.
     *
     * @return True if listener was listening and if we were searching at the time of calling
     */
    public static boolean cancelFindLocation(OnLocationFoundListener listener)
    {
        mLock.lock();

        boolean cancelled = false;
        if(mIsSearching)
        {
            if(removeListener(listener))
            {
                Log.d(LOG_TAG, "Search canceled for 1 listener.");
                cancelled = true;

                // If that was the last listener, cancel the search
                if(mLocationFoundListeners.size() == 0)
                {
                    mLock.requestEndFindLocation(null);
                }
            }
            else
            {
                Log.d(LOG_TAG, "Not cancelling search for listener: it wasn't listening!");
            }
        }
        else
        {
            Log.d(LOG_TAG, "Not cancelling search for listener: we weren't searching.");
        }

        mLock.unlock();
        return cancelled;
    }

    /**
     * Tells whether or not we are searching for the location.
     *
     * @return True if we are searching for the location, false if not.
     */
    public static boolean isSearching()
    {
        return mIsSearching;
    }

    /**
     * @return The LocationManager we are working with
     */
    public static LocationManager getLocationManager()
    {
        return mLocationManager;
    }

    /**
     * @return The Context we got the LocationManager from
     */
    public static Context getContext()
    {
        return mContext;
    }


    //---------PRIVATE METHODS---------//

    /**
     * Registers an OnLocationFoundListener to receive a notification when the
     * device's Location is found.
     *
     * @param listener The OnLocationFoundListener that we want to add to our list
     * @return True if listener was added, false if not or if it was already listening
     */
    private static boolean addListener(OnLocationFoundListener listener)
    {
        for(OnLocationFoundListener l : mLocationFoundListeners)
        {
            if(l == listener)
                return false;
        }
        mLocationFoundListeners.add(listener);
        return true;
    }

    /**
     * Removes an OnLocationFoundListener from our list so that it no longer will
     * receive a call to onLocationFound(Location).
     *
     * @param listener The OnLocationFoundListener that we want to remove from our list
     * @return True if listener was removed, false if it wasn't there to begin with
     */
    private static boolean removeListener(OnLocationFoundListener listener)
    {
        for(int i = 0; i < mLocationFoundListeners.size(); i++)
        {
            if(mLocationFoundListeners.get(i) == listener)
            {
                mLocationFoundListeners.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called when either a Location
     * is found or the timer times out. It stops the search
     * by detaching mLocationChangedListener from the location updates
     * and notifies all the OnLocationFoundListeners with a call to
     * locationFound(Location).
     *
     * Synchronized because it will be called as a result of the
     * asynchronous connection to location updates
     *
     * invariant pre-condition: mLocationFoundListeners.size() > 0
     *
     * @param location The Location we found, or null if we timed out.
     */
    private static synchronized void endFindLocation(Location location)
    {
        mLock.lock();

        Log.d(LOG_TAG, "Ending find location.");

        // No longer searching
        mIsSearching = false;

        // Kill the stop timer if it's still going
        if(mStopTimer != null)
        {
            mStopTimer.cancel();
            mStopTimer = null;
        }

        // Detach from location updates.
        if(isLocationServicesAvailable())
        {
            mLocationManager.removeUpdates(mLocationChangedListener);
        }
        else
        {
            // That is really weird...
            Log.w(LOG_TAG, "Cannot detach from location updates: " +
                    "Location Services are unavailable.");
        }

        int numListeners = mLocationFoundListeners.size();
        if(numListeners > 0)
        {
            Log.d(LOG_TAG, "Notifying " + numListeners + " listeners...");
        }
        else
        {
            Log.w(LOG_TAG, "No OnLocationFoundListeners to notify.");
        }

        // Notify all the OnLocationFoundListeners who are still waiting
        for(OnLocationFoundListener listener : mLocationFoundListeners)
        {
            listener.onLocationFound(location);
        }

        // Get rid of all the listeners
        mLocationFoundListeners.clear();

        mLock.unlock();
    }


    //---------PUBLIC INNER CLASSES AND INTERFACES---------//

    public interface OnLocationFoundListener
    {
        /**
         * This callback is fired when either the device's location is
         * found or the timer times out.
         *
         * @param location The location we found, if found, otherwise null
         */
        void onLocationFound(Location location);
    }


    //---------PRIVATE INNER CLASSES AND INTERFACES---------//

    /**
     * This class is used to protect against asynchronous calls from
     * the subscription to location updates. We should never call
     * endFindLocation() if we are in the process of:
     *      Beginning a search
     *      Adding or removing a listener
     *      Ending a search
     */
    private static class Lock
    {
        private boolean locked;
        private boolean pendingRequest;
        private Location pendingValue;

        public Lock()
        {
            locked = false;
            pendingRequest = false;
            pendingValue = null;
        }

        public synchronized void lock()
        {
            Log.d(LOG_TAG, "Lock!");
            locked = true;
        }

        public synchronized boolean isLocked()
        {
            return locked;
        }

        public synchronized boolean hasPendingRequest()
        {
            return pendingRequest;
        }

        /**
         * Unlock the lock, and call endFindLocation()
         * if we have a pending request
         */
        public synchronized void unlock()
        {
            Log.d(LOG_TAG, "Unlock!");
            locked = false;
            if(pendingRequest)
            {
                Log.d(LOG_TAG, "Had pending request.");
                endFindLocation(pendingValue);
                pendingRequest = false;
                pendingValue = null;
            }
        }

        /**
         * If we are locked, make a note that we have a pending
         * request (if there isn't already one).
         * If unlocked, go ahead and call endFindLocation()
         */
        public synchronized void requestEndFindLocation(Location location)
        {
            Log.d(LOG_TAG, "Requesting end search.");
            if(locked)
            {
                Log.d(LOG_TAG, "Waiting.");
                if(!pendingRequest)
                {
                    pendingRequest = true;
                    pendingValue = location;
                }
            }
            else
            {
                Log.d(LOG_TAG, "No need to wait.");
                endFindLocation(location);
            }
        }
    }

    /**
     * This is the class we use to attach to location updates from
     * the LocationManager
     */
    private static class MyLocationChangedListener implements LocationListener
    {
        /**
         * This is the signal we are waiting for. Once we have
         * received this once, our search is over.
         * Synchronized because this method will be called
         * from another thread.
         *
         * @param location The new Location
         */
        @Override
        public synchronized void onLocationChanged(Location location)
        {
            // We are done searching, we have found the location.
            Log.d(LOG_TAG, "Location Found: " + location.toString());

            // We must REQUEST and end to make sure we don't interrupt anything
            mLock.requestEndFindLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }
    }
}
