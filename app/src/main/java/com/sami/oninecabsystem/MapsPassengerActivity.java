package com.sami.oninecabsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
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

import com.bumptech.glide.Glide;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.sami.oninecabsystem.DialogClasses.DriverHereDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsPassengerActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, RoutingListener {
    private Button actionButton, dismissButton;
    private TextView driverUsername, driverName,driverRatingOver5;
    private ImageView driverProfilePicture;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private Location mlastKnownLocation;
    private LocationCallback locationCallback;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private final float DEFAULT_ZOOM = 18;
    private String UID, driverID;
    private boolean isSearching = false;
    private boolean driverFound = false;
    private double searchRadius = 1;
    private LatLng PickupLocation, DestinationLatLng;
    private Marker mDriverMarker, PickupMarker, DestinationMarker;
    private GeoQuery geoQuery;
    DialogFragment DriverHereDialog = new DriverHereDialog();
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private LinearLayout driverInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_passenger);
        setupViews();
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        UID = fbUser.getUid();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsPassengerActivity.this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();
        Places.initialize(MapsPassengerActivity.this, "AIzaSyD7wlRZjbXfIe03sR3xDN-C0EvkVqZq6k0");

        placesClient = Places.createClient(MapsPassengerActivity.this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();


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

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverInfo.setVisibility(View.GONE);
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Location location = mlastKnownLocation;
                PickupLocation = new LatLng(location.getLatitude(), location.getLongitude());

                if (!isSearching) {
                    DestinationLatLng = mMap.getCameraPosition().target;
                    double destLat = DestinationLatLng.latitude;
                    double destLng = DestinationLatLng.longitude;
                    HashMap<String, Object> DestinationHashMap = new HashMap<>();
                    DestinationHashMap.put("DestinationLat", destLat);
                    DestinationHashMap.put("DestinationLng", destLng);
                    DestinationMarker = mMap.addMarker(new MarkerOptions().position(DestinationLatLng).title("Destination"));
                            //.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_destination)));
                    DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("RideRequests");
                    locRef.child(UID).setValue(DestinationHashMap);
                    GeoFire geoFire = new GeoFire(locRef);
                    geoFire.setLocation(UID, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            actionButton.setText(R.string.CancelCab);
                            Toast.makeText(MapsPassengerActivity.this, "Finding a ride, please wait...", Toast.LENGTH_SHORT).show();
                            PickupMarker = mMap.addMarker(new MarkerOptions().position(PickupLocation).title("Pickup here"));
                            getClosestDriver();
                        }
                    });
                    isSearching = true;
                } else {
                    Toast.makeText(MapsPassengerActivity.this, "Cancelling your ride, Please wait...", Toast.LENGTH_SHORT).show();
                    geoQuery.removeAllListeners();
                    if (driverLocationRefListener != null)
                        driverLocationRef.removeEventListener(driverLocationRefListener);
                    DatabaseReference locRef = FirebaseDatabase.getInstance().getReference("RideRequests");
                    GeoFire geoFire = new GeoFire(locRef);
                    geoFire.removeLocation(UID, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            actionButton.setText(R.string.FindACab);
                            PickupMarker.remove();
                            searchRadius = 1;
                        }
                    });
                    locRef.child(UID).removeValue();
                    if (driverID != null) {
                        DatabaseReference DriversWorkingRef = FirebaseDatabase.getInstance().getReference("DriversWorking").child(driverID);
                        DriversWorkingRef.removeValue();
                        driverID = null;
                    }
                    if (mDriverMarker != null)
                        mDriverMarker.remove();
                    if (PickupMarker != null)
                        PickupMarker.remove();
                    if (DestinationMarker != null)
                        DestinationMarker.remove();
                    driverFound = false;
                    isSearching = false;
                }
            }

        });
    }

    public void setupViews() {
        actionButton = findViewById(R.id.btnAction);
        materialSearchBar = findViewById(R.id.searchBar);
        driverInfo = findViewById(R.id.driverInfo);
        driverUsername = findViewById(R.id.driverInfoUsername);
        driverName = findViewById(R.id.driverInfoName);
        driverProfilePicture = findViewById(R.id.driverInfoImageView);
        dismissButton = findViewById(R.id.btnDismissDriver);
        driverRatingOver5=findViewById(R.id.driverInfoRating);
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

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapsPassengerActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapsPassengerActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapsPassengerActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapsPassengerActivity.this, 51);
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
                    Toast.makeText(MapsPassengerActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
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

    }

    public void getClosestDriver() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        GeoFire findDriverGeoFire = new GeoFire(driverRef);
        geoQuery = findDriverGeoFire.queryAtLocation(new GeoLocation(PickupLocation.latitude, PickupLocation.longitude), searchRadius);
        geoQuery.removeAllListeners();
                if (isSearching) {
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            if (!driverFound) {
                                driverFound = true;
                                driverID = key;
                                DatabaseReference isWorkingRef = FirebaseDatabase.getInstance().getReference("DriversWorking").child(key);
                                HashMap<String, Object> updateVal = new HashMap<>();
                                updateVal.put("CustomerID", UID);
                                isWorkingRef.updateChildren(updateVal);
                                actionButton.setText(R.string.LocatingDriver);
                                getDriverLocation();
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            if (!driverFound) {
                                searchRadius++;
                                getClosestDriver();
                            }
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });

                }



    }

    public void getDriverInfo() {
        driverInfo.setVisibility(View.VISIBLE);
        DatabaseReference newDriverRef = FirebaseDatabase.getInstance().getReference().child("Users").child(driverID);
        final String[] driverusername = new String[1];
        final String[] driverFN = new String[1];
        final String[] driverRating= new String[1];
        final String[] driverLN = new String[1];
        final String[] driverRatingCount=new String[1];
        final String[] driverMN = new String[1];
        final String[] driverPPURL = new String[1];

        newDriverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> driverDetails = (Map<String, Object>) dataSnapshot.getValue();
                    driverusername[0] = driverDetails.get("Username").toString();
                    driverFN[0] = driverDetails.get("FirstName").toString();
                    driverLN[0] = driverDetails.get("LastName").toString();
                    driverMN[0] = driverDetails.get("MiddleName").toString();
                    driverRating[0]= driverDetails.get("Rating").toString();
                    driverRatingCount[0]= driverDetails.get("RatingCount").toString();
                    if(driverDetails.get("ProfilePictureURL")!=null)
                    driverPPURL[0] = driverDetails.get("ProfilePictureURL").toString();
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
                if (driverMN[0].equals("-"))
                    driverName.setText(String.format("%s %s", driverFN[0], driverLN[0]));
                else driverName.setText(String.format("%s %s %s", driverFN[0], driverMN[0], driverLN[0]));
                if (driverPPURL[0] != null)
                    Glide.with(getApplication()).load(driverPPURL).into(driverProfilePicture);
                driverUsername.setText(driverusername[0]);
                String ratingString = getString(R.string.rating) + driverRating[0] + getString(R.string.over5) + driverRatingCount[0] + getString(R.string.ratings);
                driverRatingOver5.setText(ratingString);

            }
        }, 2000);

    }

    public void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference("DriversAvailable").child(driverID).child("l");
       //TO-DO getDriverInfo();
                driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Object> driverCoordinates = (List<Object>) dataSnapshot.getValue();
                            double driverLocationLat = 0;
                            double driverLocationLng = 0;
                            if (driverCoordinates.get(0) != null) {
                                driverLocationLat = Double.parseDouble(driverCoordinates.get(0).toString());
                            }
                            if (driverCoordinates.get(1) != null) {
                                driverLocationLng = Double.parseDouble(driverCoordinates.get(1).toString());
                            }
                            LatLng DriverLocation = new LatLng(driverLocationLat, driverLocationLng);
                            if (mDriverMarker != null) {
                                mDriverMarker.remove();
                            }

                            Location pickupLocation = new Location("");
                            pickupLocation.setLatitude(PickupLocation.latitude);
                            pickupLocation.setLongitude(PickupLocation.longitude);

                            Location driverLocation = new Location("");
                            driverLocation.setLatitude(DriverLocation.latitude);
                            driverLocation.setLongitude(DriverLocation.longitude);

                            float driverDistance = pickupLocation.distanceTo(driverLocation);
                            if (driverDistance < 100) {
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(500);
                                DriverHereDialog.show(getSupportFragmentManager(), "AlertDialog");

                            } else
                                actionButton.setText(String.format("%s%s", getString(R.string.DriverFoundDistance), driverDistance));

                            mDriverMarker = mMap.addMarker(new MarkerOptions().position(DriverLocation).title("Driver Location"));
                            //   .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_driver)));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



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
        FirebaseDatabase.getInstance().getReference().child("RideRequests").removeValue();
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

    }

    @Override
    public void onRoutingCancelled() {

    }
}
