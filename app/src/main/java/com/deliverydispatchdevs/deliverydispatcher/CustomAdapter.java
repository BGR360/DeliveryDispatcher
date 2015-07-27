package com.deliverydispatchdevs.deliverydispatcher;
import android.content.Context;
import android.text.Layout;
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

    public CustomAdapter(Context context)
    {
        super(context, R.layout.custom_row, (List)OrderManager.getUndispatchedOrders());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View customView;
        if(convertView == null)
        {
            LayoutInflater saadInflater = LayoutInflater.from(getContext());
            customView = saadInflater.inflate(R.layout.custom_row, parent, false);
        }
        else
        {
            customView = convertView;
        }

        DeliveryOrder orderForThisRow = ((ArrayList<DeliveryOrder>) OrderManager.getUndispatchedOrders()).get(position);


        TextView orderNumView = (TextView) customView.findViewById(R.id.OrderNumView);
        TextView addressView = (TextView) customView.findViewById(R.id.AddressView);
        TextView dueTimeView = (TextView) customView.findViewById(R.id.DueTimeView);

        orderNumView.setText((Integer.toString(orderForThisRow.getOrderNumber())));
        addressView.setText(orderForThisRow.getDestination().toString());
        dueTimeView.setText(orderForThisRow.getPromisedTime().toString());

        return customView;


    }
}
