package com.sami.oninecabsystem.DialogClasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.sami.oninecabsystem.MainActivity;

public class EmailVerificationDialog extends DialogFragment {

    private FirebaseAuth fbAuth= FirebaseAuth.getInstance();
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Registration Successful");
        builder.setMessage("Please check your email address to verify your email address");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just
                // want it dismissed when clicked
                fbAuth.signOut();
                startActivity(new Intent(getContext(), MainActivity.class));

            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
