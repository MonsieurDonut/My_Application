package com.sami.oninecabsystem.DialogClasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.sami.oninecabsystem.MainDriverActivity;
import com.sami.oninecabsystem.MainPassengerActivity;


public class ChooseSignInUserDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select User");
        builder.setMessage("Choose the type of user you want to sign in as");
        builder.setPositiveButton("Passenger", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just
                // want it dismissed when clicked
                startActivity(new Intent(getContext(), MainPassengerActivity.class));
                getActivity().finish();
            }
        });
        builder.setNegativeButton("Driver", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just
                // want it dismissed when clicked
                startActivity(new Intent(getContext(), MainDriverActivity.class));
                getActivity().finish();
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}