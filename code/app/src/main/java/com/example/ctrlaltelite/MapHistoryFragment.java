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
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapHistoryFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Username of the logged-in user
     */
    private String Username;
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private String moodFilter = "Mood";
    private static final String TAG = "MapHistoryFragment";
    private MoodEvent latestMoodEvent;  // Store the single latest event
    // Filter variables as class fields
    private boolean weekFilter = false; // Default: all time
    private String reasonFilter = "";   // Default: no search

    /**
     * Public setter methods for filters (optional, if you want to set them programmatically)
     */
    public void setMoodFilter(String filter) {
        this.moodFilter = filter;
    }

    public void setWeekFilter(boolean filter) {
        this.weekFilter = filter;
    }

    public void setReasonFilter(String filter) {
        this.reasonFilter = filter;
    }

    /**
     * Provide a public getter for the latest mood event
     */
    public MoodEvent getLatestMoodEvent() {
        return latestMoodEvent;
    }

    public MapHistoryFragment() {
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

        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
            Log.d(TAG, "Username retrieved in onCreate: " + Username);
        }
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
        View view = inflater.inflate(R.layout.fragment_map_history, container, false);

        // Ensure Username is set
        if (Username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Example UI setup
        Spinner moodFilterSpinner = view.findViewById(R.id.mood_filter_history);
        CheckBox weekFilterCheckBox = view.findViewById(R.id.show_past_week_history);
        EditText reasonFilterEditText = view.findViewById(R.id.search_mood_reason_history);

        // Mood filter spinner setup
        List<String> moodOptions = Arrays.asList(getResources().getStringArray(R.array.mood_options));
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, moodOptions);
        moodFilterSpinner.setAdapter(moodAdapter);
        moodFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moodFilter = moodOptions.get(position);
                Log.d(TAG, "Mood filter updated: " + moodFilter);
                GeoPoint userLocation = getUserLocation();
                if (userLocation != null) {
                    showMoodEventMap(Username, userLocation); // Refresh map
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Week filter checkbox
        weekFilterCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekFilter = isChecked;
            Log.d(TAG, "Week filter updated: " + weekFilter);
            GeoPoint userLocation = getUserLocation();
            if (userLocation != null) {
                showMoodEventMap(Username, userLocation); // Refresh map
            }
        });

        // Reason filter edit text
        reasonFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reasonFilter = s.toString().trim();
                Log.d(TAG, "Reason filter updated: " + reasonFilter);
                GeoPoint userLocation = getUserLocation();
                if (userLocation != null) {
                    showMoodEventMap(Username, userLocation); // Refresh map
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

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
                getChildFragmentManager().findFragmentById(R.id.id_map_history);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapHistoryFragment is null.");
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

        // Load the markers for the user
        showMoodEventMap(Username, currentGeoPoint);
    }

    /**
     * Given a username and current location, fetches mood events for that user only,
     * applies filters independently, and places markers for all matching events.
     */
    void showMoodEventMap(String username, GeoPoint currentGeoPoint) {
        // Clear existing markers to avoid duplicates
        googleMap.clear();
        Log.d(TAG, "Fetching mood events for user: " + username);

        // Query Firebase for Mood Events for the specific user only
        db.collection("Mood Events")
                .whereEqualTo("username", username) // Only fetch events for this user
                .whereEqualTo("public", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodEvent> allMoodEvents = new ArrayList<>();
                        latestMoodEvent = null; // Reset the latest event

                        // Populate allMoodEvents and find the latest event
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            try {
                                MoodEvent moodEvent = documentSnapshot.toObject(MoodEvent.class);
                                allMoodEvents.add(moodEvent); // Add to full list for filtering
                                Log.d(TAG, "Fetched mood event: " + moodEvent.getEmotionalState() + " at " + moodEvent.getFormattedTimestamp());

                                // Determine the latest mood event (before filtering)
                                if (latestMoodEvent == null) {
                                    latestMoodEvent = moodEvent;
                                } else if (moodEvent.getTimestamp() != null && latestMoodEvent.getTimestamp() != null) {
                                    if (moodEvent.getTimestamp().toDate().getTime() > latestMoodEvent.getTimestamp().toDate().getTime()) {
                                        latestMoodEvent = moodEvent;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document: " + documentSnapshot.getId(), e);
                            }
                        }

                        Log.d(TAG, "Total mood events fetched: " + allMoodEvents.size());
                        if (allMoodEvents.isEmpty()) {
                            Toast.makeText(getContext(), "No public mood events found for this user", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Apply filters independently
                        List<MoodEvent> filteredMoodEvents = new ArrayList<>();
                        boolean anyFilterActive = false;

                        // Mood filter
                        if (!moodFilter.equals("Mood")) {
                            anyFilterActive = true;
                            List<MoodEvent> moodMatches = allMoodEvents.stream()
                                    .filter(event -> event.getEmotionalState() != null && event.getEmotionalState().trim().equals(moodFilter.trim()))
                                    .collect(Collectors.toList());
                            filteredMoodEvents.addAll(moodMatches);
                            Log.d(TAG, "Mood filter matches: " + moodMatches.size() + " events");
                        }

                        // Week filter
                        if (weekFilter) {
                            anyFilterActive = true;
                            Calendar oneWeekAgo = Calendar.getInstance();
                            oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7);
                            long oneWeekAgoMillis = oneWeekAgo.getTimeInMillis();
                            List<MoodEvent> weekMatches = allMoodEvents.stream()
                                    .filter(event -> event.getTimestamp() != null && event.getTimestamp().toDate().getTime() >= oneWeekAgoMillis)
                                    .collect(Collectors.toList());
                            // Add only if not already in the list to avoid duplicates
                            for (MoodEvent event : weekMatches) {
                                if (!filteredMoodEvents.contains(event)) {
                                    filteredMoodEvents.add(event);
                                }
                            }
                            Log.d(TAG, "Week filter matches: " + weekMatches.size() + " events");
                        }

                        // Reason filter
                        if (!reasonFilter.isEmpty()) {
                            anyFilterActive = true;
                            List<MoodEvent> reasonMatches = allMoodEvents.stream()
                                    .filter(event -> event.getReason() != null && Arrays.asList(event.getReason().toLowerCase().split("\\s+"))
                                            .contains(reasonFilter.toLowerCase()))
                                    .collect(Collectors.toList());
                            // Add only if not already in the list to avoid duplicates
                            for (MoodEvent event : reasonMatches) {
                                if (!filteredMoodEvents.contains(event)) {
                                    filteredMoodEvents.add(event);
                                }
                            }
                            Log.d(TAG, "Reason filter matches: " + reasonMatches.size() + " events");
                        }

                        // If no filters are active, use all events
                        if (!anyFilterActive) {
                            filteredMoodEvents = new ArrayList<>(allMoodEvents);
                            Log.d(TAG, "No filters active, using all events: " + filteredMoodEvents.size());
                        }

                        Log.d(TAG, "Total events after filtering: " + filteredMoodEvents.size());

                        // Place markers for all filtered events
                        if (!filteredMoodEvents.isEmpty()) {
                            for (MoodEvent moodEvent : filteredMoodEvents) {
                                if (moodEvent.getLocation() != null) {
                                    try {
                                        double latitude = moodEvent.getLocation().getLatitude();
                                        double longitude = moodEvent.getLocation().getLongitude();
                                        LatLng moodLocation = new LatLng(latitude, longitude);

                                        String[] moodDesc = moodEvent.getEmotionalState().split(" ");
                                        String emoji = moodDesc.length > 0 ? moodDesc[0] : "";

                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .position(moodLocation)
                                                .icon(getMarkerIcon(emoji));

                                        Marker marker = googleMap.addMarker(markerOptions);
                                        marker.setTag(moodEvent);
                                        Log.d(TAG, "Added marker for mood: " + moodEvent.getEmotionalState() + " at " + moodLocation);
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error adding marker for mood event", e);
                                    }
                                }
                            }
                            // Center on the latest event if available
                            if (latestMoodEvent != null && latestMoodEvent.getLocation() != null) {
                                LatLng latestLocation = new LatLng(latestMoodEvent.getLocation().getLatitude(), latestMoodEvent.getLocation().getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latestLocation, 15));
                            }
                        } else {
                            Toast.makeText(getContext(), "No mood events match the current filters", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "No events after filtering");
                        }
                    } else {
                        Log.e(TAG, "Error getting Mood Events", task.getException());
                        Toast.makeText(getContext(), "Error fetching mood events", Toast.LENGTH_SHORT).show();
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