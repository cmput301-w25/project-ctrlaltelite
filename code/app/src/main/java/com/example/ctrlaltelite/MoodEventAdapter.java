package com.example.ctrlaltelite;

import android.content.Context;
import android.util.Log;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;

/**
 * Custom adapter for displaying MoodEvent objects in a ListView.
 * This adapter binds mood event details, including mood, reason, trigger, timestamp,
 * social situation, and an optional image.
 */

public class MoodEventAdapter extends ArrayAdapter<MoodEvent> {
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public MoodEventAdapter(Context context, List<MoodEvent> moodEvents) {
        super(context, 0, moodEvents);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mood_event_item, parent, false);
        }

        MoodEvent moodEvent = getItem(position);

        TextView moodText = convertView.findViewById(R.id.mood_text);
        TextView reasonText = convertView.findViewById(R.id.reason_text);
        TextView triggerText = convertView.findViewById(R.id.trigger_text);
        TextView socialSituationText = convertView.findViewById(R.id.social_situation_text);
        TextView timestampText = convertView.findViewById(R.id.timestamp_text);
        TextView geolocationText = convertView.findViewById(R.id.geolocation);
        ImageView moodImage = convertView.findViewById(R.id.mood_image);

        moodText.setText(moodEvent.getEmotionalState());
        reasonText.setText(moodEvent.getReason() != null ? moodEvent.getReason() : "");
        triggerText.setText(moodEvent.getTrigger() != null ? moodEvent.getTrigger() : "");
        socialSituationText.setText(moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "");
        timestampText.setText(moodEvent.getFormattedTimestamp());

        moodText.setTextColor(getColorForMood(moodEvent.getEmotionalState()));


        //Convert Coordinates to Address
        if (moodEvent.getLocation() != null) {
            double latitude = moodEvent.getLocation().getLatitude();
            double longitude = moodEvent.getLocation().getLongitude();

            // Call a method to get address from coordinates
            String address = getAddressFromCoordinates(latitude, longitude);
            if (address != null) {
                geolocationText.setText("\uD83D\uDCCC" + address);
                geolocationText.setVisibility(View.VISIBLE);
                // Apply Gradient Background Styling
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,

                        new int[]{Color.parseColor("#FFE9DE"), Color.parseColor("#FDBEA6"), Color.parseColor("#FF9671")}


                );
                gradientDrawable.setCornerRadius(16); // Rounded corners
                Typeface customFont = ResourcesCompat.getFont(this.getContext(), R.font.font7);
                geolocationText.setTypeface(customFont, Typeface.BOLD);

                geolocationText.setBackground(gradientDrawable);
                geolocationText.setTextColor(Color.BLACK); // White text for contrast
                geolocationText.setPadding(12, 6, 12, 6); // Better spacing
                geolocationText.setTypeface(null, Typeface.BOLD); // Bold text
            }

            else {
                geolocationText.setText("at Unknown Location");
                geolocationText.setVisibility(View.VISIBLE);
            }
        } else {
            geolocationText.setVisibility(View.GONE);
        }


        if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
            StorageReference imageRef = storageRef.child(moodEvent.getImgPath());
            Glide.with(getContext()).clear(moodImage); // Clear previous image to avoid recycling issues
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Log.d("MoodEventAdapter", "Image URL fetched for " + moodEvent.getImgPath() + ": " + uri.toString());
                        Glide.with(getContext())
                                .load(uri)
                                .into(moodImage);
                        moodImage.setVisibility(View.VISIBLE); // Explicitly set visible on success
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MoodEventAdapter", "Failed to fetch image URL for " + moodEvent.getImgPath() + ": " + e.getMessage());
                        moodImage.setVisibility(View.GONE); // Hide on failure
                    });
        } else {
            Log.d("MoodEventAdapter", "No imgPath for mood event at position " + position);
            Glide.with(getContext()).clear(moodImage); // Clear image if no path
            moodImage.setVisibility(View.GONE); // Hide if no image
        }
        return convertView;
    }



    // Convert Coordinates to Address (Using Geocoder)
    private String getAddressFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0); // Get full address
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Return null if address couldn't be found
        return null;
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
            default: return 0xFF616161; // Default Gray
        }



}
}