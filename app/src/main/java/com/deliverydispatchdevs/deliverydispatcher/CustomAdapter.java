package com.deliverydispatchdevs.deliverydispatcher;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deliverydispatchdevs.deliverydispatcher.order.DeliveryOrder;
import com.deliverydispatchdevs.deliverydispatcher.order.OrderManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class CustomAdapter extends ArrayAdapter<DeliveryOrder>
{
    public CustomAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapter(Context context, int resource, List<DeliveryOrder> items) {
        super(context, resource, items);
    }


}
