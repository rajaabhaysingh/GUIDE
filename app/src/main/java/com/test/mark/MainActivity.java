package com.test.mark;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Camera;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FrameLayout toolbar;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;

    private MaterialSearchBar materialSearchBar;
    private View mapView;

    //demo find button
    private ImageButton btnFind;

    private final float DEFAULT_ZOOM = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //making status bar transparent 25B4B4B4

        // Create/Set toolbar as actionbar
        toolbar = (FrameLayout) findViewById(R.id.toolbar_container);

        // Check if the version of Android is Lollipop or higher
        if (Build.VERSION.SDK_INT >= 21) {

            // Set the status bar to dark-semi-transparent
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Set paddingTop of toolbar to height of status bar.
            // Fixes statusbar covers toolbar issue
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        }


        materialSearchBar = findViewById(R.id.searchBar);
        btnFind = findViewById(R.id.btnFind);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapView = mapFragment.getView();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        Places.initialize(MainActivity.this, "AIzaSyD66Fg36iZmtI08p5Ij3R_gN0T_xKyCFoQ");
        placesClient = Places.createClient(this);
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
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION)
                {
                    //NAVIGATION DRAWER
                }
                else if (buttonCode == MaterialSearchBar.BUTTON_BACK)
                {
                    materialSearchBar.clearSuggestions();
                    materialSearchBar.disableSearch();
                }
                else if (buttonCode == MaterialSearchBar.BUTTON_SPEECH)
                {
                    //openVoiceRecognizer();
                }
            }
        });

        //searching for places and inflating predictions
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("in")
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful())
                        {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null)
                            {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionList = new ArrayList<>();
                                for (int i=0; i<predictionList.size(); i++)
                                {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionList);
                                if (!materialSearchBar.isSuggestionsVisible())
                                {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        }else {
                            Toast.makeText(MainActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                            Log.d("Prediction", "prediction fetching unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                //use placeID to get Lat and Long and use that Lat/Long to go to selected location
                if (position >= predictionList.size())
                {
                    return;
                }

                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                //closing keyboard
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                {
                    inputMethodManager.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                }

                String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.d("place found", "place was found : " + place.getName());
                        LatLng latLng = place.getLatLng();
                        if (latLng != null)
                        {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        }
                        materialSearchBar.clearSuggestions();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException)
                        {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Toast.makeText(MainActivity.this, "place not found", Toast.LENGTH_SHORT).show();
                            Log.d("Place not found", "Place not found" + e.getMessage() + " status code : " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
                materialSearchBar.clearSuggestions();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_items, menu);

        MenuItem item = menu.findItem(R.id.action_search);

        return true;
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        //changing the default position of current location button
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null)
        {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 20, 332);
        }

        //check if GPS is enabled or not before moving user to his location on map
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(200);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException)
                {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(MainActivity.this, 100);
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
                {
                    materialSearchBar.clearSuggestions();
                    materialSearchBar.disableSearch();
                }

                return false;
            }
        });
    }

    //here we are trying to know whether user enabled the location in above steps or denied
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100)
        {
            if (resultCode == RESULT_OK)
            {
                getDeviceLocation();
            }
        }
    }

    //function to access current device location
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful())
                        {
                            mLastKnownLocation = task.getResult();

                            //but again it may return last known location as null
                            //so

                            if (mLastKnownLocation != null)
                            {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                            else {
                                LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(500);
                                locationRequest.setFastestInterval(250);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);

                                        if (locationResult == null)
                                        {
                                            return;
                                        }

                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };

                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}