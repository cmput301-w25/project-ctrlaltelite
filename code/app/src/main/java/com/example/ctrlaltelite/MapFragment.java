package com.example.ctrlaltelite;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap googleMap;
    private static final String TAG = "MapFragment";

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
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

                    // Description: Reason, Trigger, Social Situation, Timestamp
                    TextView description = new TextView(getContext());
                    StringBuilder descBuilder = new StringBuilder();
                    if (moodEvent.getReason() != null && !moodEvent.getReason().isEmpty()) {
                        descBuilder.append("Reason: ").append(moodEvent.getReason()).append("\n");
                    }
                    if (moodEvent.getTrigger() != null && !moodEvent.getTrigger().isEmpty()) {
                        descBuilder.append("Trigger: ").append(moodEvent.getTrigger()).append("\n");
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

                            Marker marker = googleMap.addMarker(markerOptions);
                            marker.setTag(moodEvent);
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
