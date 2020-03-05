package com.sami.oninecabsystem.DialogClasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.sami.oninecabsystem.NewDriverActivity;
import com.sami.oninecabsystem.NewPassengerActivity;

public class SignOutCheckDialog extends DialogFragment {
    private DialogFragment SignOutDialog=new SignOutDialog();
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just
                // want it dismissed when clicked
                FirebaseAuth fbAuth=FirebaseAuth.getInstance();
                fbAuth.signOut();
                SignOutDialog.show(getFragmentManager(), "AlertDialog");

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just
                // want it dismissed when clicked


            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}

