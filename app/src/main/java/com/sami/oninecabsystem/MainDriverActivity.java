package com.sami.oninecabsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sami.oninecabsystem.DialogClasses.SignOutCheckDialog;
import com.sami.oninecabsystem.FragmentClasses.ChatFragment;
import com.sami.oninecabsystem.FragmentClasses.MapsDriverFragment;
import com.sami.oninecabsystem.FragmentClasses.ProfileFragment;
import com.sami.oninecabsystem.FragmentClasses.SettingsFragment;
import com.sami.oninecabsystem.FragmentClasses.TripsFragment;

import java.util.Map;


public class MainDriverActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private MaterialToolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private DialogFragment SignOutCheckDialog = new SignOutCheckDialog();
    private TextView headUsername, headName;
    private ImageView ProfilePictureView;
    private DatabaseReference UserRef;
    private FirebaseAuth fbAuth;
    private String UID, Username, FirstName, LastName, MiddleName, ProfilePictureURL = null;
    private Uri ProfilePictureUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_login_main);
        createViews();
        fbAuth = FirebaseAuth.getInstance();
        UID = fbAuth.getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(UID);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(MainDriverActivity.this);
        View headerView = navigationView.getHeaderView(0);
        headUsername = headerView.findViewById(R.id.HeaderUsername);
        headName = headerView.findViewById(R.id.HeaderName);
        ProfilePictureView = headerView.findViewById(R.id.navPP);
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                Username = map.get("Username").toString();
                FirstName = map.get("FirstName").toString();
                LastName = map.get("LastName").toString();
                MiddleName = map.get("MiddleName").toString();
                headUsername.setText(Username);

                if (MiddleName.equals("-"))
                    headName.setText(String.format("%s %s", FirstName, LastName));
                else headName.setText(String.format("%s %s %s", FirstName, MiddleName, LastName));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(UID).child("ProfilePictureURL");
        UserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProfilePictureURL=dataSnapshot.getValue(String.class);
                if(ProfilePictureURL!=null){
                    Glide.with(getApplication()).load(ProfilePictureURL).into(ProfilePictureView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainDriverActivity.this, drawer, toolbar,
                R.string.navigationDrawerOpen, R.string.navigationDrawerClose);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new TripsFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_trips);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_chat:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ChatFragment()).commit();
                break;

            case R.id.nav_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                break;
            case R.id.nav_trips:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new TripsFragment()).commit();
                break;
            case R.id.nav_map:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapsDriverFragment()).commit();
                break;
            case R.id.nav_logout:
                SignOutCheckDialog.show(getSupportFragmentManager(), "AlertDialog");
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }


    public void createViews() {
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

    }
}
