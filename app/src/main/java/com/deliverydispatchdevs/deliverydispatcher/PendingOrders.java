package com.deliverydispatchdevs.deliverydispatcher;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.deliverydispatchdevs.deliverydispatcher.order.Address;
import com.deliverydispatchdevs.deliverydispatcher.order.DeliveryOrder;
import com.deliverydispatchdevs.deliverydispatcher.order.OrderManager;
import com.deliverydispatchdevs.deliverydispatcher.order.Time;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class PendingOrders extends AppCompatActivity {

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        Address address1 = new Address(("2581 Eldorado Ln"));
        Time time1 = new Time(13,12,11);

        Address address2 = new Address("Something Bennington Ct.");
        Time time2 = new Time(2, 24, 26);

        DeliveryOrder order1 = new DeliveryOrder(1, address1, time1);
        DeliveryOrder order2 =  new DeliveryOrder(2, address2, time2);
        DeliveryOrder order3 = new DeliveryOrder(3, new Address("123 Blah Dr."), new Time(12,4)); // 12:04:00 PM
        DeliveryOrder order4 = new DeliveryOrder(4, new Address("32 hell circle"), new Time(0, 0, 3)); // 12:00:03 AM
        DeliveryOrder order5 = new DeliveryOrder(5, new Address("111 whatcha doin"), new Time(22,44,12)); // 10:44:12 PM
        DeliveryOrder order6 = new DeliveryOrder(6, new Address("123 lmao ct."), new Time(1,24)); // 01:24:00 AM
        DeliveryOrder order7 = new DeliveryOrder(7, new Address("656 enough is enough dr."), new Time(23,44,32)); // 11:44:32 PM

        OrderManager.placeOrder(order1);
        OrderManager.placeOrder(order2);
        OrderManager.placeOrder(order3);
        OrderManager.placeOrder(order4);
        OrderManager.placeOrder(order5);
        OrderManager.placeOrder(order6);
        OrderManager.placeOrder(order7);





        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<DeliveryOrder> arrayAdapter = new CustomAdapter(this);
        ListView saadsView = (ListView) findViewById(R.id.listView);
        saadsView.setAdapter(arrayAdapter);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pending_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy()
    {
        OrderManager.clearAll();
        super.onDestroy();
    }
}
