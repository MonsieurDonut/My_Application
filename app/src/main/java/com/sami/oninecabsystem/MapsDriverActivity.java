package com.sami.oninecabsystem;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MapsDriverActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    private Button actionButton, dismissButton;
    private TextView passengerUsername, passengerName;
    private ImageView passengerProfilePicture;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private Location mlastKnownLocation;
    private LocationCallback locationCallback;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private final float DEFAULT_ZOOM = 10;
    private String UID, CustomerID;
    private boolean isAvailable = false;
    private Marker mCustomerMarker;
    private DatabaseReference CustomerLocationRef;
    private ValueEventListener CustomerLocationRefListener;
    private LinearLayout passengerInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_driver);
        setupViews();
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        UID = fbUser.getUid();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsDriverActivity.this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        Places.initialize(MapsDriverActivity.this, "AIzaSyD7wlRZjbXfIe03sR3xDN-C0EvkVqZq6k0");

        placesClient = Places.createClient(MapsDriverActivity.this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        actionButton.setText(R.string.setAvailable);
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                    materialSearchBar.clearSuggestions();
                }

            }

        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("MU")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }

                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("Location", "Prediction fetching task unsuccessful");
                        }
                    }
                });

                materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
                    @Override
                    public void OnItemClickListener(int position, View v) {
                        if (position >= predictionList.size())
                            return;
                        AutocompletePrediction selectedPrediction = predictionList.get(position);
                        String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                        materialSearchBar.setText(suggestion);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                materialSearchBar.clearSuggestions();
                            }
                        }, 1000);


                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        if (imm != null)
                            imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                        String placeID = selectedPrediction.getPlaceId();
                        List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);

                        FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeID, placeFields).build();
                        placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                            @Override
                            public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                Place place = fetchPlaceResponse.getPlace();
                                LatLng latLngofplace = place.getLatLng();
                                if (latLngofplace != null) {
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngofplace, DEFAULT_ZOOM));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (e instanceof ApiException) {
                                    ApiException apiException = (ApiException) e;
                                    apiException.printStackTrace();
                                    int statusCode = apiException.getStatusCode();
                                    Log.i("Location", "Place not found : " + statusCode);
                                }
                            }
                        });


                    }

                    @Override
                    public void OnItemDeleteListener(int position, View v) {

                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                materialSearchBar.clearSuggestions();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = mlastKnownLocation;
                if (!isAvailable) {
                    DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                    GeoFire geoFire = new GeoFire(locRef);
                    geoFire.setLocation(UID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            actionButton.setText(R.string.resetAvailable);
                        }
                    });
                    isAvailable = true;
                } else {
                    DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("DriversAvailable");
                    GeoFire geoFire = new GeoFire(locRef);
                    geoFire.removeLocation(UID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            actionButton.setText(R.string.setAvailable);
                        }
                    });
                    locRef.child(UID).removeValue();
                    isAvailable = false;
                }
            }

        });
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passengerInfo.setVisibility(View.GONE);
            }
        });
        getAssignedCustomer();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);


        }

        //checking if gps is enabled and prompting user to enable if not
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapsDriverActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapsDriverActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapsDriverActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapsDriverActivity.this, 51);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (materialSearchBar.isSuggestionsVisible())
                    materialSearchBar.clearSuggestions();
                if (materialSearchBar.isSearchEnabled())
                    materialSearchBar.disableSearch();
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }

    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mlastKnownLocation = task.getResult();
                    if (mlastKnownLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mlastKnownLocation.getLatitude(), mlastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                    } else {
                        final LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setInterval(1000);
                        locationRequest.setFastestInterval(1000);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);

                                if (locationResult == null)
                                    return;
                                mlastKnownLocation = locationResult.getLastLocation();
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mlastKnownLocation.getLatitude(), mlastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                mFusedLocationProviderClient.removeLocationUpdates(locationCallback);


                            }
                        };
                        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Toast.makeText(MapsDriverActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        mlastKnownLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));

        DatabaseReference driversWorkingRef = FirebaseDatabase.getInstance().getReference("DriversWorking");
        DatabaseReference driversAvailableRef = FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire driversWorkingGeoFire = new GeoFire(driversWorkingRef);
        GeoFire driversAvailableGeoFire = new GeoFire(driversAvailableRef);

        if (CustomerID.isEmpty()) {
            driversWorkingRef.removeValue();
            driversAvailableGeoFire.setLocation(UID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        } else {
            driversAvailableRef.removeValue();
            driversWorkingGeoFire.setLocation(UID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                }
            });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire geoFire = new GeoFire(locRef);
        geoFire.removeLocation(UID, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
    }

    public void getAssignedCustomer() {
        DatabaseReference customerIDRef = FirebaseDatabase.getInstance().getReference("DriversWorking").child(UID).child("CustomerID");
        CustomerLocationRefListener = customerIDRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && isAvailable) {
                    Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    CustomerID = dataSnapshot.getValue().toString();
                    actionButton.setText(R.string.CustomerFound);
                    v.vibrate(500);
                    getAssignedCustomerPickupLocation();
                } else {
                    CustomerID = null;
                    if (CustomerLocationRefListener != null) {
                        if (CustomerLocationRef != null)
                            CustomerLocationRef.removeEventListener(CustomerLocationRefListener);
                    }
                    if (mCustomerMarker != null)
                        mCustomerMarker.remove();
                    if (isAvailable)
                        actionButton.setText(R.string.resetAvailable);
                    else actionButton.setText(R.string.setAvailable);
                    passengerInfo.setVisibility(View.GONE);
                    FirebaseDatabase.getInstance().getReference().child("DriversAvailable").child(UID).removeValue();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void getAssignedCustomerPickupLocation() {
        CustomerLocationRef = FirebaseDatabase.getInstance().getReference("RideRequests").child(CustomerID).child("l");
        CustomerLocationRefListener = CustomerLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !(CustomerID.isEmpty())) {
                    List<Object> CustomerLocationList = (List<Object>) dataSnapshot.getValue();
                    double CustomerLocationLat = 0;
                    double CustomerLocationLng = 0;
                    if (CustomerLocationList.get(0) != null) {
                        CustomerLocationLat = Double.parseDouble(CustomerLocationList.get(0).toString());
                    }
                    if (CustomerLocationList.get(1) != null) {
                        CustomerLocationLng = Double.parseDouble(CustomerLocationList.get(1).toString());
                    }
                    LatLng CustomerLocation = new LatLng(CustomerLocationLat, CustomerLocationLng);
                    if (mCustomerMarker != null) {
                        mCustomerMarker.remove();
                    } else
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(CustomerLocation));
                    mMap.addMarker(new MarkerOptions().position(CustomerLocation).title("Pickup Location"));
                    getPassengerInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getPassengerInfo() {
        passengerInfo.setVisibility(View.VISIBLE);
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child(CustomerID);
        final String[] passengerusername = new String[1];
        final String[] passengerFN = new String[1];
        final String[] passengerLN = new String[1];
        final String[] passengerMN = new String[1];
        final String[] passengerPPURL = new String[1];

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> driverDetails = (Map<String, Object>) dataSnapshot.getValue();
                    passengerusername[0] = driverDetails.get("Username").toString();
                    passengerFN[0] = driverDetails.get("FirstName").toString();
                    passengerLN[0] = driverDetails.get("LastName").toString();
                    passengerMN[0] = driverDetails.get("MiddleName").toString();
                    if (driverDetails.get("ProfilePictureURL") != null)
                        passengerPPURL[0] = driverDetails.get("ProfilePictureURL").toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (passengerMN[0].equals("-"))
                    passengerName.setText(String.format("%s %s", passengerFN[0], passengerLN[0]));
                else
                    passengerName.setText(String.format("%s %s %s", passengerFN[0], passengerMN[0], passengerLN[0]));
                if (passengerPPURL[0] != null) {
                    Glide.with(getApplication()).load(passengerPPURL).into(passengerProfilePicture);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                        }
                    }, 5000);

                }
                passengerUsername.setText(passengerusername[0]);

            }
        }, 2000);

    }

    public void setupViews() {
        actionButton = findViewById(R.id.btnAction2);
        materialSearchBar = findViewById(R.id.searchBar2);
        passengerInfo = findViewById(R.id.passengerInfo);
        passengerUsername = findViewById(R.id.passengerInfoUsername);
        passengerName = findViewById(R.id.passengerInfoName);
        passengerProfilePicture = findViewById(R.id.passengerInfoImageView);
        dismissButton = findViewById(R.id.btnDismissPassenger);
    }
}
