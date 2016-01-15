package com.example.bgr.googleplacessearch;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.Places;
import com.google.maps.android.ui.IconGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapActivity extends AppCompatActivity implements
        MyLocationFinder.OnLocationFoundListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener
{
    private static final String LOG_TAG = "MapActivity";
    private static final float DEFAULT_ZOOM_LEVEL = 14.0f;
    private static final double MIN_DISTANCE_BETWEEN_LOCATIONS = 20.0;  // meters

    private MenuItem mSearchItem;
    private SearchView mSearchView;

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Marker mLocationMarker;

    private GoogleApiClient mGoogleApiClient;

    private float maxDistance = 0.0f;
    private static int mLastOrderNumber = 0;
    private IconGenerator mIconGenerator;

    private ArrayList<Marker> mMarkers;
    private ArrayList<Integer> mMarkerStyles;

    private ArrayList<Marker> mSelectedMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Set the Toolbar from our layout as our activity's ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_top);
        setSupportActionBar(toolbar);

        // If elevation is supported (API 21+), elevate the Toolbar
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            float pixels = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDimension(R.dimen.actionbar_elevation),
                    getResources().getDisplayMetrics());
            toolbar.setElevation(pixels);
        }

        mIconGenerator = new IconGenerator(this);

        acquireApiClient();
        setUpLocationServices();
        handleIntent(getIntent());
        setUpMapIfNeeded();
        findLocation();
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

    /**
     * We need this method to handle the search queries from the SearchView.
     *
     * @param intent The new Intent that our Activity has received
     */
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

        // Set up our SearchView
        setUpSearchView(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        if(id == R.id.action_change_color)
        {
            changeMarkerColor();
        }

        if(id == R.id.action_search)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //---------INTERFACE METHODS FOR GOOGLEAPICLIENT---------//

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.d(LOG_TAG, "GeoDataApi successfully connected!");
        PlaceSuggestionsProvider.initialize(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.d(LOG_TAG, "Api connection suspended: code=" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.e(LOG_TAG, connectionResult.toString());
    }

    /**
     * Builds a GoogleApiClient that we need in order to use
     * Places Autocomplete API
     */
    private void acquireApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Gets an instance of LocationManager.
     */
    private void setUpLocationServices()
    {
        MyLocationFinder.initialize(this);
    }

    /**
     * If we haven't yet received a GoogleMap object
     * from our MapFragment, get one!
     */
    private void setUpMapIfNeeded()
    {
        if(mGoogleMap == null)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapFragment);
            if(mapFragment != null)
            {
                mGoogleMap = mapFragment.getMap();

                if(mGoogleMap == null)
                {
                    Log.w(LOG_TAG, "The GoogleMap retrieved from the MapFragment is null.");
                    return;
                }

                // Set up the GoogleMap
                UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setRotateGesturesEnabled(false);
                uiSettings.setCompassEnabled(false);
                uiSettings.setMyLocationButtonEnabled(false);
                uiSettings.setMapToolbarEnabled(false);

                // Add listeners to the map
                mGoogleMap.setOnMarkerClickListener(this);
                mGoogleMap.setOnInfoWindowClickListener(this);
                mGoogleMap.setOnMapClickListener(this);
            }
            else
            {
                Log.w(LOG_TAG, "Failed to find MapFragment!");
            }
        }
    }

    /**
     * Called when the FloatingActionButton is clicked.
     *
     * @param v The View that was clicked
     */
    public void onMyLocationButtonClick(View v)
    {
        findLocation();
    }

    public void onCloseButtonClick(View v)
    {

    }

    public void onGroupButtonClick(View v)
    {

    }

    public void onRemoveButtonClick(View v)
    {

    }

    /**
     * Starts an asynchronous task to wait until our Location
     * is found. When it is found, we will call
     * centerMapOnLocation(Location)
     */
    private void findLocation()
    {
        if(MyLocationFinder.isLocationServicesAvailable())
        {
            Toast.makeText(this, "Getting your current location...", Toast.LENGTH_LONG).show();

            // First, try to get the last known location
            Location lastKnownLocation = MyLocationFinder.getLastKnownLocation();

            mLocation = lastKnownLocation;
            MyLocationFinder.beginFindLocation(this);

            if(mLocation != null)
            {
                centerMapOnMyLocation();
            }
        }
        else
        {
            Log.w(LOG_TAG, "Could not find current location: Location Services are unavailable.");
        }
    }

    @Override
    public void onLocationFound(Location location)
    {
        // Location is null if not found
        if(location == null)
        {
            if (mLocation == null)
            {
                Log.w(LOG_TAG, "Could not find current location.");
                Toast.makeText(MapActivity.this, "Could not find current location :(",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            if(mLocation != null)
            {
                // See if the new location is significantly different
                double distance = mLocation.distanceTo(location);
                if(distance > MIN_DISTANCE_BETWEEN_LOCATIONS)
                {
                    mLocation = location;
                    centerMapOnMyLocation();
                }
            }
        }
    }

    /**
     * Animates the map's camera to the specified Location
     *
     * @param location The specified Location
     */
    private void centerMapOnLocation(Location location)
    {
        if(location != null)
        {
            CameraPosition newPosition = CameraPosition.builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(DEFAULT_ZOOM_LEVEL).build();
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
        }
        else
        {
            Log.w(LOG_TAG, "Tried to center camera on a null Location.");
        }
    }

    /**
     * Animates the map's camera to the device's location and
     * places a marker there
     */
    private void centerMapOnMyLocation()
    {
        // If we already have a marker there, remove it
        if(mLocationMarker != null)
        {
            mLocationMarker.remove();
            mLocationMarker = null;
        }

        // Add Marker
        mLocationMarker = mGoogleMap.addMarker(new MarkerOptions().icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
        ).position(
                new LatLng(mLocation.getLatitude(), mLocation.getLongitude())
        ));

        // Center map on device location
        centerMapOnLocation(mLocation);
    }

    /**
     * Sets up the SearchView that we have in our ActionBar/Toolbar thing
     *
     * @param menu The Menu that holds the ActionBar's MenuItems
     */
    private void setUpSearchView(Menu menu)
    {
        // Set up the SearchView
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        if(mSearchItem == null)
        {
            mSearchItem = searchItem;
        }
        if(mSearchView == null)
        {
            mSearchView = searchView;
        }

        // Make sure SearchView is collapsed when launched
        searchView.setIconifiedByDefault(true);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        if(searchManager != null)
        {
            mSearchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
        }
        else
        {
            Log.w(LOG_TAG, "Failed to retrieve Search Services.");
        }

        //Set up listeners so that we can give SearchView focus when it is expanded
        MenuItemCompat.setOnActionExpandListener(searchItem,
                new MenuItemCompat.OnActionExpandListener()
                {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item)
                    {
                        // Do this to raise the keyboard into view
                        searchView.setIconified(false);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item)
                    {
                        // Do this to hide the keyboard from view
                        searchView.setIconified(true);
                        return true;
                    }
                });
    }

    /**
     * Checks to see if intent is a search intent, and if so,
     * opens the "New Order" dialog.
     *
     * @param intent The Intent we want to handle
     */
    private void handleIntent(Intent intent)
    {
        // Verify the action and get the query
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String addressText = intent.getStringExtra(SearchManager.QUERY);
            createNewOrder(addressText);
        }
        else if(Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            mSearchView.setQuery("", false);
            MenuItemCompat.collapseActionView(mSearchItem);

            String placeID = intent.getData().getLastPathSegment();
            Log.d(LOG_TAG, "Place ID: " + placeID);

            PendingResult<PlaceBuffer> result = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeID);
            result.setResultCallback(new ResultCallback<PlaceBuffer>()
            {
                @Override
                public void onResult(PlaceBuffer places)
                {
                    Place place = places.get(0).freeze();
                    createNewOrder(place);
                    places.release();
                }
            });
        }
    }

    /**
     * Creates a Place from addressText and opens the "New Order" dialog with it
     *
     * @param addressText The String that the user entered in the SearchView
     */
    private void createNewOrder(String addressText)
    {
        NewOrderDialogFragment dialog = new NewOrderDialogFragment();
        dialog.setAddressText(addressText);
        dialog.setListener(new NewOrderDialogFragment.NewOrderDialogListener()
        {
            @Override
            public void onDialogAccept(String address)
            {
                // Create New Order
                //Toast.makeText(MapActivity.this, "Created new order for " + address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDialogReject()
            {
                // User cancelled the dialog
                //Toast.makeText(MapActivity.this, "Canceled create new order", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getFragmentManager(), getResources().getString(R.string.dialog_new_order_tag));
    }

    private void createNewOrder(Place place)
    {
        final LatLng placePos = place.getLatLng();

        Location location = new Location("New Marker");
        location.setLatitude(placePos.latitude);
        location.setLongitude(placePos.longitude);
        centerMapOnLocation(location);

        String addressText = place.getAddress().toString();

        final NewOrderDialogFragment dialog = new NewOrderDialogFragment();
        dialog.setAddressText(addressText);
        dialog.setOrderNumber(++mLastOrderNumber);
        dialog.setListener(new NewOrderDialogFragment.NewOrderDialogListener()
        {
            @Override
            public void onDialogAccept(String address)
            {
                try
                {
                    List<Address> addresses = new Geocoder(MapActivity.this).getFromLocation(
                            placePos.latitude, placePos.longitude, 1);
                    address = addresses.get(0).getAddressLine(0);
                } catch (IOException e)
                {
                    Log.e(LOG_TAG, e.toString());
                }

                // Place Marker on map
                mIconGenerator.setStyle(IconGenerator.STYLE_WHITE);
                String orderNumber = Integer.toString(dialog.getOrderNumber());

                Marker newMarker = mGoogleMap.addMarker(new MarkerOptions()
                        .position(placePos)
                        .title(orderNumber)
                        .snippet(address)
                        .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                        mIconGenerator.makeIcon(orderNumber))
                        ));
                addMarker(newMarker, IconGenerator.STYLE_WHITE);
                MapActivity.mLastOrderNumber = dialog.getOrderNumber();
            }

            @Override
            public void onDialogReject()
            {
                // User cancelled the dialog
                //Toast.makeText(MapActivity.this, "Canceled create new order", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show(getFragmentManager(), getResources().getString(R.string.dialog_new_order_tag));
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        clearSelection();
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if(isMarkerSelected(marker))
        {
            // It was already selected, so deselect it
            deselectMarker(marker);
        }
        else
        {
            // It was not selected, so select it
            selectMarker(marker);
        }
        return true;
    }

    public void addMarker(Marker marker, int style)
    {
        mMarkers.add(marker);
        mMarkerStyles.add(style);
    }

    public void removeMarker(Marker marker)
    {
        mMarkerStyles.remove(mMarkers.indexOf(marker));
        mMarkers.remove(marker);
        marker.remove();
    }

    public boolean isMarkerSelected(Marker marker)
    {
        return mSelectedMarkers.contains(marker);
    }

    public void selectMarker(Marker marker)
    {
        mSelectedMarkers.add(marker);

        // Make the icon bigger
    }

    public void deselectMarker(Marker marker)
    {
        mSelectedMarkers.remove(marker);

        // Make the icon smaller
    }

    public BitmapDescriptor getIcon(String orderNumber, int style, int size)
    {
        int padding = 0;
        switch(size)
        {
            case 0:
                padding = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        MapActivity.this.getResources().getDimension(R.dimen.marker_padding),
                        MapActivity.this.getResources().getDisplayMetrics()
                );
                break;

            case 1:
                padding = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        MapActivity.this.getResources().getDimension(R.dimen.marker_padding),
                        MapActivity.this.getResources().getDisplayMetrics()
                ) * 2;
                break;

            default:
                padding = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        MapActivity.this.getResources().getDimension(R.dimen.marker_padding),
                        MapActivity.this.getResources().getDisplayMetrics()
                );
                break;
        }
        mIconGenerator.setContentPadding(padding, padding / 2, padding, padding / 2);
        mIconGenerator.setStyle(style);

        return BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(orderNumber));
    }

    public BitmapDescriptor getIcon(int orderNumber, int style, int size)
    {
        return getIcon(Integer.toString(orderNumber), style, size);
    }

    public BitmapDescriptor getIcon(Marker marker, int size)
    {
        String orderNumber = marker.getTitle();
        int index = mMarkers.indexOf(marker);
        return getIcon(orderNumber, mMarkerStyles.get(index), size);
    }

    public void clearSelection()
    {
        for(Marker marker : mSelectedMarkers)
        {
            marker.setIcon(getIcon(marker, 0));
        }
        mSelectedMarkers.clear();
    }

    public void removeMarkers()
    {
        final ArrayList<Marker> markers = mSelectedMarkers;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle("Remove Order?")
                .setMessage("Are you sure you want to remove this order?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        for(Marker marker : markers)
                        {
                            deselectMarker(marker);
                            removeMarker(marker);
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private static final CharSequence[] colorsList =
            {
                    "Blue", "Default", "Green", "Orange", "Purple", "Red", "White"
            };

    private void changeMarkerColor(Marker marker)
    {
        if (marker != null)
        {
            final Marker finalMarker = marker;
            final ArrayList<Marker> mMarkers = this.mMarkers;
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle("Select Color")
                    .setItems(colorsList, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case 0:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_BLUE);
                                    break;
                                case 1:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_DEFAULT);
                                    break;
                                case 2:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_GREEN);
                                    break;
                                case 3:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
                                    break;
                                case 4:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_PURPLE);
                                    break;
                                case 5:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_RED);
                                    break;
                                case 6:
                                    mIconGenerator.setStyle(IconGenerator.STYLE_WHITE);
                                    break;
                                default:
                                    break;
                            }

                            LatLng pos = finalMarker.getPosition();
                            String orderNumber = finalMarker.getTitle();
                            String addressText = finalMarker.getSnippet();

                            finalMarker.setIcon(getIcon(orderNumber, which, 0));

                            Marker newMarker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(pos)
                                    .title(orderNumber)
                                    .snippet(addressText)
                                    .icon(
                                            BitmapDescriptorFactory.fromBitmap(
                                                    mIconGenerator.makeIcon(orderNumber))
                                    ));
                            newMarker.showInfoWindow();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();
            dialog.show();
        }
        else
        {
            Toast.makeText(this, "No marker selected", Toast.LENGTH_SHORT).show();
        }
    }
}
