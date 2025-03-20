package com.example.ctrlaltelite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    /** Username of the logged-in user */
    private String Username;
    private GoogleMap googleMap;
    private static final String TAG = "MapFragment";
    private int MAX_DISTANCE = 5000;    //MAX DISTANCE IN METRES
    public static GeoPoint getUserLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // Remove previous updates if needed (optional)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return null;
        }
        // Get last known location (this may be null)
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            return new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            Toast.makeText(context, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     *
     * @param savedInstanceState The saved instance state, if available.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates and returns the view hierarchy for this fragment.
     *
     * @param inflater  The LayoutInflater to inflate the layout.
     * @param container The parent view that the fragment UI will attach to.
     * @param savedInstanceState The saved instance state, if available.
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        ImageButton buttonDrawerToggle = view.findViewById(R.id.buttonDrawerToggle);


        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();
        LottieAnimationView lottiemapclear = view.findViewById(R.id.lottiemapclear);

        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
            Log.d("MoodHistoryFragment", "Fetching mood events for Username: " + Username);
        }
        if (Username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }


        if (buttonDrawerToggle != null && mainActivity != null) {
            buttonDrawerToggle.setOnClickListener(v -> {
                mainActivity.openDrawer();
            });
        }

        //Pressing on the notification button

        LottieAnimationView notifButton = view.findViewById(R.id.notif); // Ensure it's LottieAnimationView
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Username != null) { // Ensure Username is retrieved before navigating
                    ViewFollowRequestsFragment followRequestsFragment = new ViewFollowRequestsFragment(Username);

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).fragmentRepl(followRequestsFragment);
                    }
                } else {
                    Toast.makeText(getContext(), "Error: Username not found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Attempt to get the SupportMapFragment from the child FragmentManager
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.id_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapFragment is null.");
        }
    }









    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();  // Ask for permission
            return;
        } else {
            // If permission is granted, proceed with the location setup
            proceedWithMapSetup();
        }
    }


    private void proceedWithMapSetup() {
        GeoPoint currentGeoPoint = getUserLocation(requireContext());
        if (currentGeoPoint == null) {
            Toast.makeText(getContext(),"No location retrieved",Toast.LENGTH_SHORT).show();
        }
        LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        // Set a custom info window adapter for displaying mood event details
        googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoWindow(Marker marker) {
                // Use default frame; return null so getInfoContents() is called.
                return null;
            }
            @Override
            public View getInfoContents(Marker marker) {
                // Retrieve the MoodEvent from the marker's tag
                Object tag = marker.getTag();
                if (tag instanceof MoodEvent) {
                    MoodEvent moodEvent = (MoodEvent) tag;
                    // Create a vertical LinearLayout to hold the content
                    LinearLayout infoLayout = new LinearLayout(getContext());
                    infoLayout.setOrientation(LinearLayout.VERTICAL);
                    infoLayout.setPadding(20, 20, 20, 20);

                    // Title: Emotional State (with emoji)
                    TextView title = new TextView(getContext());
                    title.setText(moodEvent.getEmotionalState());
                    title.setTextColor(Color.BLACK);
                    title.setTextSize(16);
                    title.setGravity(Gravity.CENTER);
                    infoLayout.addView(title);

                    // Description
                    TextView description = new TextView(getContext());
                    StringBuilder descBuilder = new StringBuilder();
                    if (moodEvent.getReason() != null && !moodEvent.getReason().isEmpty()) {
                        descBuilder.append("Reason: ").append(moodEvent.getReason()).append("\n");
                    }
                    if (moodEvent.getSocialSituation() != null && !moodEvent.getSocialSituation().isEmpty()) {
                        descBuilder.append("Social: ").append(moodEvent.getSocialSituation()).append("\n");
                    }
                    if (moodEvent.getTimestamp() != null) {
                        descBuilder.append("Time: ").append(moodEvent.getFormattedTimestamp());
                    }
                    description.setText(descBuilder.toString());
                    description.setTextColor(Color.DKGRAY);
                    description.setTextSize(14);
                    infoLayout.addView(description);

                    return infoLayout;
                }
                return null;
            }
        });



        //Query Firebase for Mood Events with locations
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Mood Events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                //Loop Through each mood event
                for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                    try{
                        MoodEvent moodEvent = documentSnapshot.toObject(MoodEvent.class);
                        if (moodEvent.getLocation()!= null){
                            double latitude = moodEvent.getLocation().getLatitude();
                            double longitude = moodEvent.getLocation().getLongitude();
                            LatLng moodLocation = new LatLng(latitude,longitude);

                            //Calculate distance
                            float[] results = new float[1];
                            Location.distanceBetween(currentLatLng.latitude,currentLatLng.longitude,moodLocation.latitude,moodLocation.longitude,results);
                            float distance = results[0];
                            if (distance <= MAX_DISTANCE){
                                //Extract Emoji from mood Event
                                String[] moodDesc = moodEvent.getEmotionalState().split(" ");
                                String emoji = new String();
                                if (moodDesc.length>0){
                                    emoji = moodDesc[0];
                                }else{
                                    emoji = "";
                                }
                                //Change marker to the Mood Event Emoji
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(moodLocation)
                                        .icon(getMarkerIcon(emoji));

                                Marker markers = googleMap.addMarker(markerOptions);
                                markers.setTag(moodEvent);
                            }
                        }
                    }catch (Exception e){
                        Log.e(TAG,"Error parsing Document: "+documentSnapshot.getId(),e);
                    }
                }
            } else{
                Log.e(TAG,"Error getting Mood Events",task.getException());
            }
        });
    }


    protected void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explain why permission is needed and request it again
                new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                        .setTitle("Location Permission Required")
                        .setPositiveButton("OK", (dialog, which) ->
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100))
                        .setNegativeButton("Cancel", (dialog, which) ->
                                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show())
                        .show();
            } else {
                // Directly request permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location permission granted.", Toast.LENGTH_SHORT).show();
                // Permission granted, proceed with the map setup
                proceedWithMapSetup();
            } else {
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                if (!showRationale) {
                    // User selected "Don't ask again" so redirect to settings
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                            .setTitle("Permission Required")
                            .setMessage("Location permission is necessary to use this feature. Please enable it in settings.")
                            .setPositiveButton("Go to Settings", (dialog, which) -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) ->
                                    Toast.makeText(getContext(), "Location permission denied, go to settings", Toast.LENGTH_SHORT).show())
                            .show();
                } else {
                    Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



















    /**
     * Generates a BitmapDescriptor from an emoji string to be used as a marker icon.
     * @param emoji The emoji to display.
     * @return A BitmapDescriptor representing the emoji.
     */
    private BitmapDescriptor getMarkerIcon(String emoji) {
        // Create a TextView and set the emoji text
        TextView textView = new TextView(getContext());
        textView.setText(emoji);
        textView.setTextSize(30); // Adjust the text size as needed
        textView.setTextColor(Color.BLACK);
        textView.setDrawingCacheEnabled(true);

        // Measure and layout the TextView
        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

        // Create a bitmap and draw the TextView onto the canvas
        Bitmap bitmap = Bitmap.createBitmap(textView.getMeasuredWidth(), textView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        textView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
