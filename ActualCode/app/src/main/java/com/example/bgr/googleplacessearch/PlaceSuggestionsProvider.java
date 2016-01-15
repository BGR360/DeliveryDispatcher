package com.example.bgr.googleplacessearch;

/**
 * Created by BGR on 7/17/2015.
 *
 * This class extends ContentProvider in order to provide custom search
 * suggestions to the SearchView in MapActivity. Utilizes the
 * Place Autocomplete feature of the Google Maps API for Android.
 */

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Location;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.internal.PlaceImpl;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PlaceSuggestionsProvider extends ContentProvider
{
    private static final String LOG_TAG = "Suggestions";

    /**
     * When creating a Cursor to hold our search suggestions,
     * a proper searchable configuration requires at least two
     * column names:
     *      _ID: unique row id
     *      SUGGEST_COLUMN_TEXT_1: the first line of text displayed in the suggestions dropdown
     */
    private static final String[] COLUMN_NAMES =
            {
                    BaseColumns._ID,
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
            };

    /**
     * The Places GeoDataApi requires a GoogleApiClient enabled with the GeoDataApi
     */
    private static GoogleApiClient googleApiClient;

    /**
     * The Places Autocomplete method requires a boundary within which to search,
     * specified by a LatLngBounds object. The searchRadius variable will be an
     * indication of how big we want this search area to be.
     */
    private static double searchRadius = 0.4;

    private Location mDeviceLocation;


    public static void initialize(GoogleApiClient client)
    {
        googleApiClient = client;
    }


    @Override
    public boolean onCreate()
    {
        return true;
    }

    public static void setSearchRadius(double radius)
    {
        searchRadius = radius;
    }

    public static double getSearchRadius()
    {
        return searchRadius;
    }

    /**
     * The system calls query() with a useless URI
     * with the user's text appended to the end. This text is the address we use
     * to query Places Autocomplete and generate a list of possible Places.
     *
     * @param uri Mostly useless, but has the address text at the end of it
     * @param projection Null
     * @param selection Null
     * @param selectionArgs Null
     * @param sortOrder Null
     * @return A MatrixCursor with all the Place suggestions we have to offer
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        Log.d(LOG_TAG, "Querying PlaceSugestionsProvider:");
        if(uri != null)
            Log.d(LOG_TAG, "uri = " + uri);
        if(projection != null)
            Log.d(LOG_TAG, "projection: length=" + projection.length);
        if(selection != null)
            Log.d(LOG_TAG, "selection = " + selection);
        if(selectionArgs != null)
            Log.d(LOG_TAG, "selectionArgs: length=" + selectionArgs.length);
        if(sortOrder != null)
            Log.d(LOG_TAG, "sortOrder = " + sortOrder);

        // The text that the user entered is included in the last segment of the URI
        String addressText = uri.getLastPathSegment();
        Log.d(LOG_TAG, "User input = " + addressText);

        // Sometimes, if the user has entered nothing, the last segment is "search_suggest_query"
        if(!addressText.equals("search_suggest_query"))
        {
            return generateSuggestionsCursor(addressText);
        }

        return null;
    }


    /**
     * This is where all the magic happens with Google Places API.
     * First, we query the Places Autocomplete API with a blocking
     * call (since we are already running asynchronously), and then
     * we create a MatrixCursor to hold the autocomplete results.
     *
     * @param searchText The text the user has entered so far
     * @return A Matrix Cursor that contains all the search suggestions
     */
    private Cursor generateSuggestionsCursor(String searchText)
    {
        MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);

        // We cannot proceed unless we are initialized
        if(googleApiClient != null)
        {
            AutocompletePredictionBuffer predictionBuffer =
                    getAutocompletePredictions(searchText);

            if(predictionBuffer != null)
            {
                // Sort the results by proximity
                List<DistanceIndexPair> indices = sortPredictions(predictionBuffer);

                // Fill the MatrixCursor with one row for each prediction
                int size = indices.size();
                for(int i = 0; i < size; i++)
                {
                    AutocompletePrediction prediction =
                            predictionBuffer.get(indices.get(i).index);
                    cursor.newRow()
                            .add(i)
                            .add(prediction.getDescription())
                            .add(prediction.getPlaceId());
                }

                indices.clear();
                predictionBuffer.release();
            }
        }

        return cursor;
    }

    /**
     * Calculates a LatLngBounds based on the value of searchRadius
     * @return A new LatLngBounds based on the device's Location and searchRadius
     */
    private LatLngBounds getLatLngBounds()
    {
        mDeviceLocation = MyLocationFinder.getLastKnownLocation();
        if(mDeviceLocation == null)
        {
            return new LatLngBounds(
                    new LatLng(0.0, 0.0),
                    new LatLng(0.0, 0.0)
            );
        }

        LatLng center = new LatLng(
                mDeviceLocation.getLatitude(),
                mDeviceLocation.getLongitude()
        );

        return new LatLngBounds(
                new LatLng(
                        center.latitude - searchRadius,
                        center.longitude - searchRadius
                ),
                new LatLng(
                        center.latitude + searchRadius,
                        center.longitude + searchRadius
                )
        );
    }

    /**
     * Queries the Place Autocomplete functionality from the Geodata API
     * @param searchText The text the user entered
     * @return An AutocompletePredictionBuffer that holds all of the AutocompletePredictions.
     * This buffer must be released when it is no longer needed.
     */
    private AutocompletePredictionBuffer getAutocompletePredictions(String searchText)
    {
        // Query the Places Autocomplete API
        LatLngBounds bounds = getLatLngBounds();
        Log.d(LOG_TAG, "Bounds: " + bounds.toString());

        PendingResult<AutocompletePredictionBuffer> result =
                Places.GeoDataApi.getAutocompletePredictions(
                        googleApiClient, searchText,
                        bounds, null);

        Log.d(LOG_TAG, "Awaiting predictions from GeoDataApi...");
        AutocompletePredictionBuffer buffer = result.await();

        // Confirm that the query completed successfully, otherwise return null
        final Status status = buffer.getStatus();
        if (!status.isSuccess()) {
            Toast.makeText(getContext(), "Error contacting API: " + status.toString(),
                    Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Error getting autocomplete prediction API call: " + status.toString());
            buffer.release();

            return null;
        }

        return buffer;
    }

    private List<DistanceIndexPair> sortPredictions(AutocompletePredictionBuffer buffer)
    {
        // Get the Place IDs of all the predictions in the buffer
        int size = buffer.getCount();
        String[] ids = new String[size];
        for(int i = 0; i < size; i++)
        {
            ids[i] = buffer.get(i).getPlaceId();
        }

        // Get all the Places represented by those IDs
        PendingResult result =
                Places.GeoDataApi.getPlaceById(googleApiClient, ids);
        PlaceBuffer placeBuffer = (PlaceBuffer) result.await();

        size = placeBuffer.getCount();

        List<DistanceIndexPair> indices = new ArrayList<>(size);
        for(int i = 0; i < size; i++)
        {
            float[] results = new float[1];
            Place place = placeBuffer.get(i);
            Location.distanceBetween(
                    mDeviceLocation.getLatitude(), mDeviceLocation.getLongitude(),
                    place.getLatLng().latitude, place.getLatLng().longitude,
                    results
            );

            DistanceIndexPair index = new DistanceIndexPair();
            index.distance = results[0];
            index.index = i;
            indices.add(index);
        }
        Collections.sort(indices);

        return indices;
    }



    //---------TOTALLY USELESS METHODS---------

    @Override
    public String getType(Uri uri) { return null;}

    @Override
    public Uri insert(Uri uri, ContentValues values) {return null;}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {return 0;}

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {return 0;}


    //---------PREDICTION COMPARATOR---------//

    private class DistanceIndexPair implements Comparable<DistanceIndexPair>
    {
        public double distance;
        public int index;

        @Override
        public int compareTo(DistanceIndexPair another)
        {
            if(distance > another.distance)
            {
                return 1;
            }
            else if (distance < another.distance)
            {
                return -1;
            }

            return 0;
        }
    }

    private class PredictionComparator implements Comparator<AutocompletePrediction>
    {

        @Override
        public int compare(AutocompletePrediction lhs, AutocompletePrediction rhs)
        {
            return 0;
        }
    }
}
