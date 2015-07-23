package com.deliverydispatchdevs.deliverydispatcher;

/**
 * Created by BGR on 7/22/2015.
 *
 * This class handles all the setup of the collapsible SearchView that's held in MapActivity's ActionBar
 */

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class SearchViewManager implements MenuItemCompat.OnActionExpandListener
{
    private static final String LOG_TAG = "MapsActivity";

    private Activity mActivity;
    private MenuItem mSearchItem;   // This is the MenuItem in the ActionBar
    private SearchView mSearchView;     // This is the actual SearchView

    public SearchViewManager(Activity activity, Menu menu)
    {
        mActivity = activity;
        // Retrieve the SearchView from menu
        mSearchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) mSearchItem.getActionView();

        setUpSearchView();
    }

    /**
     * Sets up the SearchView that we have in our ActionBar/Toolbar thing
     */
    private void setUpSearchView()
    {
        // Make sure SearchView is collapsed when launched
        mSearchView.setIconifiedByDefault(true);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) mActivity.getSystemService(Context.SEARCH_SERVICE);

        if(searchManager != null)
        {
            mSearchView.setSearchableInfo(
                    searchManager.getSearchableInfo(mActivity.getComponentName()));
        }
        else
        {
            Log.w(LOG_TAG, "Failed to retrieve Search Services.");
        }

        //Set up listeners so that we can give SearchView focus when it is expanded
        MenuItemCompat.setOnActionExpandListener(mSearchItem, this);
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item)
    {
        // Do this to raise the keyboard into view
        mSearchView.setIconified(false);
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item)
    {
        // Do this to hide the keyboard from view
        mSearchView.setIconified(true);
        return true;
    }
}
