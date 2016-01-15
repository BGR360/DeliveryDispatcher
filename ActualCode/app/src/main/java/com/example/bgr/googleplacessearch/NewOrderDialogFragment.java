package com.example.bgr.googleplacessearch;

/**
 * Created by BGR on 7/16/2015.
 *
 * This class represents a DialogFragment that pops up when the user creates a new DeliveryOrder
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

public class NewOrderDialogFragment extends DialogFragment
{
    private static final String LOG_TAG = "Dialog";

    private String mAddressText = "";
    private NewOrderDialogListener mListener;
    private int mOrderNumber = 0;

    private NumberPicker mOrderNumberPicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_new_order, null);
        builder.setTitle(R.string.dialog_new_order_title)
                .setMessage("At Address: " + mAddressText)
                .setPositiveButton(R.string.dialog_new_order_accept, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        if (mListener != null)
                        {
                            mListener.onDialogAccept(mAddressText);
                        }
                        else
                        {
                            Log.w(LOG_TAG, "No listener to notify.");
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_new_order_cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        if (mListener != null)
                        {
                            mListener.onDialogReject();
                        }
                        else
                        {
                            Log.w(LOG_TAG, "No listener to notify.");
                        }
                    }
                })
                .setView(dialogView);

        // Set up the contents view
        mOrderNumberPicker = (NumberPicker) dialogView.findViewById(R.id.orderNumberPicker);

        mOrderNumberPicker.setMinValue(0);
        mOrderNumberPicker.setMaxValue(700);
        mOrderNumberPicker.setValue(mOrderNumber);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setAddressText(String addressText)
    {
        mAddressText = addressText;
    }
    public void setOrderNumber(int orderNumber) { mOrderNumber = orderNumber; }
    public int getOrderNumber()
    {
        return mOrderNumberPicker.getValue();
    }

    public void setListener(NewOrderDialogListener listener)
    {
        mListener = listener;
    }

    public NewOrderDialogListener getListener()
    {
        return mListener;
    }

    public interface NewOrderDialogListener
    {
        public void onDialogAccept(String address);
        public void onDialogReject();
    }
}
