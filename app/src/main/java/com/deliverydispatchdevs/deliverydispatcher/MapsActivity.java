package com.deliverydispatchdevs.deliverydispatcher;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends GoogleApisActivity
{
    private static final String LOG_TAG = "MapsActivity";

    private GoogleMap mGoogleMap; // Might be null if Google Play services API is not available.

    private SearchViewManager mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        handleIntent(getIntent());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);

        // Set up the SearchViewManager to do its thing
        mSearchView = new SearchViewManager(this, menu);
        return true;
    }

    /**
     * This is where we ask for all the APIs we need to use in the app.
     *
     * @return A new GoogleApiClient with all the APIs.
     */
    @Override
    protected GoogleApiClient buildApiClient()
    {
        return new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    //---------PRIVATE METHODS---------//

    /**
     * This activity can be called with three possible intents:
     *      1.  The LAUNCH intent
     *      2.  The SEARCH intent
     *      3.  The VIEW intent
     * This method handles the Intent and decides how to act accordingly
     *
     * @param intent The Intent that the Activity was launched with
     */
    @SuppressWarnings("StatementWithEmptyBody")
    private void handleIntent(Intent intent)
    {
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            // The SEARCH intent means that the user pressed enter in the SearchView
        }
        else if(Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            // The VIEW intent means that the user selected one of the search suggestions
        }
        else
        {
            // It was the LAUNCH intent, we should initialize our state
            initializeActivity();
        }
    }

    /**
     * Sets up all the necessary stuff 'n things for our Activity
     * upon its creation
     */
    private void initializeActivity()
    {
        // Set the Toolbar from our layout as our activity's ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.mapToolbar);
        setSupportActionBar(toolbar);

        LocationServices.initialize(this);
        setUpMapIfNeeded();
    }

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

    /**
     * Opens the NewOrderDialog so that the user can create a new Order
     * from the address they searched.
     *
     * @param address A Location that represents the address of the order.
     */
    private void createNewOrder(Location address)
    {
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        Toast.makeText(this, "Creating a new Order at " + latLng.toString(), Toast.LENGTH_SHORT).show();
    }
}
