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
import android.text.TextWatcher;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

public class MapFollowingFragment extends Fragment implements OnMapReadyCallback {

    private String Username;
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private static final String TAG = "MapFollowingFragment";
    private Map<String, MoodEvent> latestMood = new HashMap<>();

    // Filter variables
    private String moodFilter = "Mood"; // Default: no filter
    private boolean weekFilter = false; // Default: all time
    private String reasonFilter = "";   // Default: no search

    public Map<String, MoodEvent> getMarkerMap() {
        return latestMood;
    }

    public MapFollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
            Log.d(TAG, "Username retrieved in onCreate: " + Username);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_following, container, false);

        if (Username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize filter UI components
        Spinner moodFilterSpinner = view.findViewById(R.id.mood_filter_following);
        CheckBox weekFilterCheckBox = view.findViewById(R.id.show_past_week_following);
        EditText reasonFilterEditText = view.findViewById(R.id.search_mood_reason_following);

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
                    getFollowedUsers(userLocation); // Refresh map
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
                getFollowedUsers(userLocation); // Refresh map
            }
        });

        // Reason filter edit text
        reasonFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reasonFilter = s.toString().trim().toLowerCase();
                Log.d(TAG, "Reason filter updated: " + reasonFilter);
                GeoPoint userLocation = getUserLocation();
                if (userLocation != null) {
                    getFollowedUsers(userLocation); // Refresh map
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.id_map_following);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "MapFollowingFragment is null.");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        } else {
            proceedWithMapSetup();
        }
    }

    protected GeoPoint getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            return null;
        }

        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("Location Debug", "Updated Latitude: " + latitude + ", Longitude: " + longitude);
                locationManager.removeUpdates(this);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(@NonNull String provider) {}
            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(getContext(), "GPS is turned off!", Toast.LENGTH_SHORT).show();
            }
        };
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);

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
        googleMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof MoodEvent) {
                MoodEvent moodEvent = (MoodEvent) tag;
                showMoodEventDialog(moodEvent);
            }
            return true;
        });

        getFollowedUsers(currentGeoPoint);
    }

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

    void showMoodEventMap(List<String> followed, GeoPoint currentGeoPoint) {
        googleMap.clear();
        latestMood.clear();
        Log.d(TAG, "Fetching mood events for followed users: " + followed);

        db.collection("Mood Events")
                .whereIn("username", followed)
                .whereEqualTo("public", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MoodEvent> allMoodEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            try {
                                MoodEvent moodEvent = documentSnapshot.toObject(MoodEvent.class);
                                allMoodEvents.add(moodEvent);
                                Log.d(TAG, "Fetched mood event: " + moodEvent.getEmotionalState() + " for user: " + moodEvent.getUsername());
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document: " + documentSnapshot.getId(), e);
                            }
                        }

                        if (allMoodEvents.isEmpty()) {
                            Toast.makeText(getContext(), "No public mood events from followed users", Toast.LENGTH_SHORT).show();
                            LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            return;
                        }

                        // Apply filters similar to HomeFragment's applyFilters
                        List<MoodEvent> filteredMoodEvents = new ArrayList<>(allMoodEvents);

                        boolean anyFilterActive = false;

                        if (!moodFilter.equals("Mood")) {
                            anyFilterActive = true;
                            filteredMoodEvents = filteredMoodEvents.stream()
                                    .filter(event -> event.getEmotionalState() != null && event.getEmotionalState().trim().equals(moodFilter.trim()))
                                    .collect(Collectors.toList());
                            Log.d(TAG, "After mood filter: " + filteredMoodEvents.size() + " events");
                        }

                        if (weekFilter) {
                            anyFilterActive = true;
                            Calendar oneWeekAgo = Calendar.getInstance();
                            oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7);
                            long oneWeekAgoMillis = oneWeekAgo.getTimeInMillis();
                            filteredMoodEvents = filteredMoodEvents.stream()
                                    .filter(event -> event.getTimestamp() != null && event.getTimestamp().toDate().getTime() >= oneWeekAgoMillis)
                                    .collect(Collectors.toList());
                            Log.d(TAG, "After week filter: " + filteredMoodEvents.size() + " events");
                        }

                        if (!reasonFilter.isEmpty()) {
                            anyFilterActive = true;
                            filteredMoodEvents = filteredMoodEvents.stream()
                                    .filter(event -> event.getReason() != null && Arrays.asList(event.getReason().toLowerCase().split("\\s+"))
                                            .contains(reasonFilter))
                                    .collect(Collectors.toList());
                            Log.d(TAG, "After reason filter: " + filteredMoodEvents.size() + " events");
                        }

                        // Populate latestMood with the latest event per user from filtered results
                        for (MoodEvent moodEvent : filteredMoodEvents) {
                            String user = moodEvent.getUsername();
                            if (!latestMood.containsKey(user)) {
                                latestMood.put(user, moodEvent);
                            } else {
                                MoodEvent previousMood = latestMood.get(user);
                                if (moodEvent.getTimestamp() != null && previousMood != null && previousMood.getTimestamp() != null) {
                                    if (moodEvent.getTimestamp().toDate().getTime() > previousMood.getTimestamp().toDate().getTime()) {
                                        latestMood.put(user, moodEvent);
                                    }
                                }
                            }
                        }

                        if (latestMood.isEmpty()) {
                            Toast.makeText(getContext(), "No mood events match the current filters", Toast.LENGTH_SHORT).show();
                            LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            return;
                        }

                        // Place markers for the latest filtered events
                        MoodEvent latestFilteredEvent = null;
                        for (MoodEvent moodEvent : latestMood.values()) {
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
                                    Log.d(TAG, "Added marker for " + moodEvent.getUsername() + ": " + moodEvent.getEmotionalState() + " at " + moodLocation);

                                    // Determine the latest event with a location
                                    if (latestFilteredEvent == null || (moodEvent.getTimestamp() != null && latestFilteredEvent.getTimestamp() != null &&
                                            moodEvent.getTimestamp().toDate().getTime() > latestFilteredEvent.getTimestamp().toDate().getTime())) {
                                        latestFilteredEvent = moodEvent;
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error adding marker for mood event", e);
                                }
                            }
                        }

                        // Center the map based on filter status
                        if (anyFilterActive && latestFilteredEvent != null && latestFilteredEvent.getLocation() != null) {
                            LatLng latestEventLatLng = new LatLng(latestFilteredEvent.getLocation().getLatitude(), latestFilteredEvent.getLocation().getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latestEventLatLng, 15));
                            Log.d(TAG, "Centered map on latest filtered event: " + latestFilteredEvent.getEmotionalState() + " at " + latestEventLatLng);
                        } else {
                            // Default: center on user's current location when no filters are active
                            LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            Log.d(TAG, "Centered map on user's location: " + currentLatLng);
                        }
                    } else {
                        Log.e(TAG, "Error getting Mood Events", task.getException());
                        Toast.makeText(getContext(), "Error fetching mood events", Toast.LENGTH_SHORT).show();
                        LatLng currentLatLng = new LatLng(currentGeoPoint.getLatitude(), currentGeoPoint.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    }
                });
    }

    protected void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                        .setTitle("Location Permission Required")
                        .setPositiveButton("OK", (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100))
                        .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show())
                        .show();
            } else {
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
                proceedWithMapSetup();
            } else {
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                if (!showRationale) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
                            .setTitle("Permission Required")
                            .setMessage("Location permission is necessary to use this feature. Please enable it in settings.")
                            .setPositiveButton("Go to Settings", (dialog, which) -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(getContext(), "Location permission denied, go to settings", Toast.LENGTH_SHORT).show())
                            .show();
                } else {
                    Toast.makeText(getContext(), "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private BitmapDescriptor getMarkerIcon(String emoji) {
        TextView textView = new TextView(getContext());
        textView.setText(emoji);
        textView.setTextSize(30);
        textView.setTextColor(Color.BLACK);
        textView.setDrawingCacheEnabled(true);

        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());

        Bitmap bitmap = Bitmap.createBitmap(textView.getMeasuredWidth(), textView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        textView.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showMoodEventDialog(MoodEvent moodEvent) {
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
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(moodEvent.getImgPath());
            ref.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(getContext()).load(uri).into(image))
                    .addOnFailureListener(e -> image.setVisibility(View.GONE));
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
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{Color.parseColor("#FFE9DE"), Color.parseColor("#FDBEA6"), Color.parseColor("#FF9671")}
                );
                gradientDrawable.setCornerRadius(16);
                Typeface customFont = ResourcesCompat.getFont(this.getContext(), R.font.font7);
                geoTextView.setTypeface(customFont, Typeface.BOLD);
                geoTextView.setBackground(gradientDrawable);
                geoTextView.setTextColor(Color.BLACK);
                geoTextView.setPadding(12, 6, 12, 6);
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
                        String displayName = task.getResult().getDocuments().get(0).getString("displayName");
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