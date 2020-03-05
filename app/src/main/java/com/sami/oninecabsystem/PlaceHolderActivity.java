package com.sami.oninecabsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sami.oninecabsystem.DialogClasses.ChooseSignInUserDialog;

public class PlaceHolderActivity extends AppCompatActivity {
    private FirebaseAuth fbAuth = FirebaseAuth.getInstance();
    DialogFragment ChooseSignInUser = new ChooseSignInUserDialog();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(fbAuth.getCurrentUser().getUid()).child("UserType");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userType = dataSnapshot.getValue(String.class);
                if (userType == null) {
                    startActivity(new Intent(PlaceHolderActivity.this, MainActivity.class));
                    fbAuth.signOut();
                }

                if (userType.equals("Passenger"))
                    startActivity(new Intent(PlaceHolderActivity.this, MainPassengerActivity.class));

                else
                    ChooseSignInUser.show(getSupportFragmentManager(), "AlertTag");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setContentView(R.layout.activity_place_holder);

    }
}
