package com.deliverydispatchdevs.deliverydispatcher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends GoogleApisActivity
{
    private static final String LOG_TAG = "MapsActivity";

    private GoogleMap mGoogleMap; // Might be null if Google Play services APK is not available.

    private SearchViewManager mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Set the Toolbar from our layout as our activity's ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        setSupportActionBar(toolbar);

        setUpMapIfNeeded();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onStop()
    {
        Log.d(LOG_TAG, "Disconnecting from Google Apis.");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        mSearchView = new SearchViewManager(this, menu);
        return true;
    }

    @Override
    protected GoogleApiClient buildApiClient()
    {
        return new GoogleApiClient.Builder(this)
                .build();
    }

    //---------PRIVATE METHODS---------//

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mGoogleMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    @SuppressWarnings("Convert2Lambda")
    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mGoogleMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            // Use getMapAsync() is the recommended way to get a map.
            ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment))
                    .getMapAsync(new OnMapReadyCallback()
                    {
                        @Override
                        public void onMapReady(GoogleMap googleMap)
                        {
                            mGoogleMap = googleMap;

                            // Check if we were successful in obtaining the map.
                            if (mGoogleMap != null)
                            {
                                setUpMap();
                            }
                        }
                    });

        }
    }

    /**
     * This is where we can add markers or lines, add listeners, move the camera,
     * or set up the map's UI settings.
     * <p/>
     * This should only be called once and when we are sure that {@link #mGoogleMap} is not null.
     */
    private void setUpMap()
    {
        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("A Different Title"));
    }
}
