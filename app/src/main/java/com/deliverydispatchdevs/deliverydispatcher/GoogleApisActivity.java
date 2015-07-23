package com.deliverydispatchdevs.deliverydispatcher;

/**
 * Created by BGR on 7/22/2015.
 *
 * This class is the base class for an Activity
 * which utilizes one or more Google APIs.
 * Subclasses must override ac
 */

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public abstract class GoogleApisActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final String LOG_TAG = "GoogleApiClient";

    protected GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = buildApiClient();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        Log.d(LOG_TAG, "Disconnecting from Google APIs...");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public GoogleApiClient getApiClient()
    {
        return mGoogleApiClient;
    }

    /**
     * Override this method to return a new GoogleApiClient
     * that includes the APIs that your application requires.
     * Use GoogleApiClient.Builder() to create this client.
     *
     * @return A new GoogleApiClient
     */
    protected abstract GoogleApiClient buildApiClient();


    //---------INTERFACE METHODS FOR GOOGLEAPICLIENT---------//

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(LOG_TAG, "Google APIs successfully connected!");

        // We have to initialize the Suggestions Provider before it will function
        PlaceSuggestionsProvider.initialize(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(LOG_TAG, "API connection suspended: code=" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.e(LOG_TAG, connectionResult.toString());
    }
}
