package com.example.ctrlaltelite;

import static android.view.View.GONE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFollowingFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Username of the logged-in user
     */
    private String Username;
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private static final String TAG = "MapNearbyFragment";
    private int MAX_DISTANCE = 5000;    //MAX DISTANCE IN METRES
    //Hash Map to store latest mood event per user
    private Map<String, MoodEvent> latestMood = new HashMap<>();

    /**
     * Provide a public getter so we can assert marker contents in tests.
     */
    public Map<String, MoodEvent> getMarkerMap() {
        return latestMood;
    }

    public MapFollowingFragment() {
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
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Creates and returns the view hierarchy for this fragment.
     *
     * @param inflater           The LayoutInflater to inflate the layout.
     * @param container          The parent view that the fragment UI will attach to.
     * @param savedInstanceState The saved instance state, if available.
     * @return The root view of the fragment's layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_nearby, container, false);


        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();


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


        return view;
    }


    /**
     * Once the view is created, set up the Google Map fragment.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Attempt to get the SupportMapFragment from the child FragmentManager
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.id_map_nearby);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapNearbyFragment is null.");
        }
    }

    /**
     * Called when the map is ready to be used.
     */
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

    /**
     * Retrieves the user's current location as a GeoPoint (if permission granted).
     */
    protected GeoPoint getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            return null;
        }

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        // Local LocationListener instance.
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("Location Debug", "Updated Latitude: " + latitude + ", Longitude: " + longitude);

                locationManager.removeUpdates(this);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(getContext(), "GPS is turned off!", Toast.LENGTH_SHORT).show();
            }
        };
        // Request a single update using listener.
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);

        // Get the last known location as a fallback.
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            Log.d("Location Debug", "Last Known Latitude: " + latitude + ", Longitude: " + longitude);
            return new GeoPoint(latitude, longitude);
        } else {
            Toast.makeText(getContext(), "Unable to get updated location", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Actual setup for the map once permissions are granted.
     */
    private void proceedWithMapSetup() {
        GeoPoint currentGeoPoint = getUserLocation();
        if (currentGeoPoint == null) {
            Toast.makeText(getContext(), "No location retrieved", Toast.LENGTH_SHORT).show();
            HomeFragment homeFragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", Username);
            homeFragment.setArguments(bundle);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).fragmentRepl(homeFragment);
            }
            return;
        }
        LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        // Open AlertBox to show Mood Event details when clicked on a Marker
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof MoodEvent) {
                MoodEvent moodEvent = (MoodEvent) tag;
                showMoodEventDialog(moodEvent);
            }
            // Handled Click
            return true;
        });

        // Load the markers from all followed users.
        getFollowedUsers(currentGeoPoint);
    }


    /**
     * Fetches the list of usernames that 'Username' follows (with Status=Accepted).
     */
    private void getFollowedUsers(GeoPoint currentGeoPoint) {
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", Username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> followedUsers = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String followed = documentSnapshot.getString("Requestee's Username");
                        if (followed != null) {
                            followedUsers.add(followed);
                        }
                    }
                    if (followedUsers.isEmpty()) {
                        Toast.makeText(getContext(), "You Don't Follow Anyone", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showMoodEventMap(followedUsers, currentGeoPoint);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Could not get follow requests", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Given a list of followed usernames, fetch their mood events and place markers.
     */
    void showMoodEventMap(List<String> followed, GeoPoint currentGeoPoint) {
        //Query Firebase for Mood Events with locations
        db.collection("Mood Events").whereIn("username", followed).whereEqualTo("public", true).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Loop Through each mood event
                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                    try {
                        MoodEvent moodEvent = documentSnapshot.toObject(MoodEvent.class);
                        String user = moodEvent.getUsername();

                        if (!latestMood.containsKey(user)) {
                            latestMood.put(user, moodEvent);
                        } else {
                            MoodEvent previousMood = latestMood.get(user);
                            if (moodEvent.getTimestamp() != null) {
                                assert previousMood != null;
                                if (previousMood.getTimestamp() != null) {
                                    if (moodEvent.getTimestamp().toDate().getTime() > previousMood.getTimestamp().toDate().getTime()) {
                                        latestMood.put(user, moodEvent);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting document: " + documentSnapshot.getId(), e);
                    }
                }
                LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                for (MoodEvent moodEvent : latestMood.values()) {
                    try {
                        if (moodEvent.getLocation() != null) {
                            double latitude = moodEvent.getLocation().getLatitude();
                            double longitude = moodEvent.getLocation().getLongitude();
                            LatLng moodLocation = new LatLng(latitude, longitude);

                            //Calculate distance
                            float[] results = new float[1];
                            Location.distanceBetween(currentLatLng.latitude, currentLatLng.longitude, moodLocation.latitude, moodLocation.longitude, results);
                            float distance = results[0];
                            if (distance <= MAX_DISTANCE) {
                                //Extract Emoji from mood Event
                                String[] moodDesc = moodEvent.getEmotionalState().split(" ");
                                String emoji = new String();
                                if (moodDesc.length > 0) {
                                    emoji = moodDesc[0];
                                } else {
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
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting Mood from hash map", e);
                    }
                }
            } else {
                Log.e(TAG, "Error getting Mood Events", task.getException());
            }
        });
    }

    /**
     * Helper method to request location permission, if not granted.
     */
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

    /**
     * Called when user responds to the location permission dialog.
     */
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
     *
     * @param emoji The emoji to display.
     * @return A BitmapDescriptor representing the emoji.
     */
    private BitmapDescriptor getMarkerIcon(String emoji) {
        // Create a TextView and set the emoji text
        TextView textView = new TextView(getContext());
        textView.setText(emoji);
        textView.setTextSize(30);
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


    /**
     * Converts latitude and longitude to an address string.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The address corresponding to the latitude and longitude, or null if no address is found.
     */
    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);  // Get full address
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return null if address couldn't be found
    }


    /**
     * Opens an AlertDialog that shows the mood event details,
     * including the real image loaded from Firebase Storage using Glide.
     */
    private void showMoodEventDialog(MoodEvent moodEvent) {
        // Inflate a layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.mood_event_item, null);
        dialogView.setBackgroundColor(Color.TRANSPARENT);
        TextView emotionalState = dialogView.findViewById(R.id.mood_text);
        TextView displayNameView = dialogView.findViewById(R.id.display_name);
        TextView reason = dialogView.findViewById(R.id.reason_text);
        TextView socialSituation = dialogView.findViewById(R.id.social_situation_text);
        TextView timestamp = dialogView.findViewById(R.id.timestamp_text);
        TextView geoTextView = dialogView.findViewById(R.id.geolocation);
        ImageView image = dialogView.findViewById(R.id.mood_image);
        ImageButton commentsButton = dialogView.findViewById(R.id.comments_button);
        commentsButton.setVisibility(View.GONE);


        emotionalState.setText(moodEvent.getEmotionalState());
        emotionalState.setTextColor(getColorForMood(moodEvent.getEmotionalState()));

        reason.setText(moodEvent.getReason());
        socialSituation.setText(moodEvent.getSocialSituation());
        timestamp.setText(moodEvent.getFormattedTimestamp());

        if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference()
                    .child(moodEvent.getImgPath());

            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(getContext()).load(uri).into(image);
            }).addOnFailureListener(e -> {
                image.setVisibility(View.GONE);
            });
        } else {
            image.setVisibility(View.GONE);
        }


        if (moodEvent.getLocation() != null) {
            double latitude = moodEvent.getLocation().getLatitude();
            double longitude = moodEvent.getLocation().getLongitude();

            String address = getAddressFromCoordinates(latitude, longitude);

            if (address != null) {
                geoTextView.setText("\uD83D\uDCCC" + address);
                geoTextView.setVisibility(View.VISIBLE);
                // Apply Gradient Background Styling
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{Color.parseColor("#FFE9DE"), Color.parseColor("#FDBEA6"), Color.parseColor("#FF9671")}
                );
                gradientDrawable.setCornerRadius(16); // Rounded corners
                Typeface customFont = ResourcesCompat.getFont(this.getContext(), R.font.font7);
                geoTextView.setTypeface(customFont, Typeface.BOLD);
                geoTextView.setBackground(gradientDrawable);
                geoTextView.setTextColor(Color.BLACK); // White text for contrast
                geoTextView.setPadding(12, 6, 12, 6); // Better spacing
            } else {
                geoTextView.setText("at Unknown Location");
                geoTextView.setVisibility(View.VISIBLE);

            }
        } else {
            geoTextView.setVisibility(View.GONE);
        }



        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("username", moodEvent.getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the first matched document
                        String displayName = task.getResult()
                                .getDocuments()
                                .get(0)
                                .getString("displayName");
                        displayNameView.setText(displayName);

                        new AlertDialog.Builder(requireContext())
//                                .setTitle(displayName != null ? displayName : "Mood Event")
                                .setView(dialogView)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    } else {
                        Log.e("Firestore", "No matching user found or error: ", task.getException());
                        new AlertDialog.Builder(requireContext())
                                .setTitle("@" + moodEvent.getUsername())
                                .setView(dialogView)
                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }
                });

    }



    /**
     * Determines the text color for the mood text based on the mood type.
     *
     * @param mood The mood description.
     * @return The corresponding color value.
     */
    private int getColorForMood(String mood) {
        switch (mood) {
            case "😊 Happy": return 0xFFFFC107; // Amber
            case "😢 Sad": return 0xFF2196F3; // Blue
            case "😲 Surprised": return 0xFFFF5722; // Orange
            case "😡 Angry": return 0xFFD32F2F; // Red
            case "🤢 Disgust": return 0xFF4CAF50; // Green
            case "😕 Confusion": return 0xFF9C27B0; // Purple
            case "😨 Fear": return 0xFF3F51B5;  // Indigo
            case "😳 Shame": return 0xFFFF9800; // Orange
            default: return 0xFF616161; // Default Gray
        }
    }

}
