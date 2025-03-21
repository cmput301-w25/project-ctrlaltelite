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
 * Custom Adapter for displaying mood events in a ListView.
 * This adapter binds data from a list of MoodEvent objects to the ListView items.
 * It handles setting text, loading images from Firebase Storage, and displaying geolocation information.
 */

public class MoodEventAdapter extends ArrayAdapter<MoodEvent> {
    private FirebaseStorage storage;
    private StorageReference storageRef;

    /**
     * Constructor for the MoodEventAdapter.
     *
     * @param context The context in which the adapter is created
     * @param moodEvents The list of MoodEvent objects to be displayed.
     */
    public MoodEventAdapter(Context context, List<MoodEvent> moodEvents) {
        super(context,
                0,
                moodEvents);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }


    /**
     * ViewHolder class to cache view references and improve performance by avoiding
     * repeated calls to findViewById.
     */
    // The ViewHolder is used to improve the performance of ListView.
    // It helps avoid repeatedly finding views during scrolling by holding references to the UI components
    static class ViewHolder {
        TextView moodText;
        TextView reasonText;
        TextView socialSituationText;
        TextView timestampText;
        TextView geolocationText;
        ImageView moodImage;
    }


    /**
     * Called to create or reuse a view for an item in the list. This method binds the data
     * from the MoodEvent object to the views in the layout.
     *
     * @param position The position of the item in the list.
     * @param convertView The recycled view that can be reused (or null if no view is available).
     * @param parent The parent ViewGroup that will contain the returned view.
     * @return The view representing the list item at the given position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.mood_event_item, parent, false);
            holder = new ViewHolder();
            holder.moodText = convertView.findViewById(R.id.mood_text);
            holder.reasonText = convertView.findViewById(R.id.reason_text);
            holder.socialSituationText = convertView.findViewById(R.id.social_situation_text);
            holder.timestampText = convertView.findViewById(R.id.timestamp_text);
            holder.geolocationText = convertView.findViewById(R.id.geolocation);
            holder.moodImage = convertView.findViewById(R.id.mood_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoodEvent moodEvent = getItem(position);
        if (moodEvent == null) return convertView;

        // Bind data to views
        holder.moodText.setText(moodEvent.getEmotionalState());
        holder.reasonText.setText(moodEvent.getReason() != null ? moodEvent.getReason() : "");
        holder.socialSituationText.setText(moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "");
        holder.timestampText.setText(moodEvent.getFormattedTimestamp());
        holder.moodText.setTextColor(getColorForMood(moodEvent.getEmotionalState()));

        // Clear image for recycled views
        Glide.with(getContext()).clear(holder.moodImage);
        holder.moodImage.setVisibility(View.GONE); // Hide initially


        //Convert Coordinates to Address
        if (moodEvent.getLocation() != null) {
            double latitude = moodEvent.getLocation().getLatitude();
            double longitude = moodEvent.getLocation().getLongitude();

            // Call a method to get address from coordinates
            String address = getAddressFromCoordinates(latitude, longitude);
            if (address != null) {
                holder.geolocationText.setText("\uD83D\uDCCC" + address);
                holder.geolocationText.setVisibility(View.VISIBLE);
                // Apply Gradient Background Styling
                GradientDrawable gradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,

                        new int[]{Color.parseColor("#FFE9DE"), Color.parseColor("#FDBEA6"), Color.parseColor("#FF9671")}


                );
                gradientDrawable.setCornerRadius(16); // Rounded corners
                Typeface customFont = ResourcesCompat.getFont(this.getContext(), R.font.font7);
                holder.geolocationText.setTypeface(customFont, Typeface.BOLD);

                holder.geolocationText.setBackground(gradientDrawable);
                holder.geolocationText.setTextColor(Color.BLACK); // White text for contrast
                holder.geolocationText.setPadding(12, 6, 12, 6); // Better spacing

            }

            else {
                holder.geolocationText.setText("at Unknown Location");
                holder.geolocationText.setVisibility(View.VISIBLE);
            }
        } else {
            holder.geolocationText.setVisibility(View.GONE);
        }


        String currentImgPath = moodEvent.getImgPath();
        holder.moodImage.setTag(currentImgPath);
        // Load image using Glide if available
        if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
            StorageReference imageRef = storageRef.child(moodEvent.getImgPath());
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Log.d("MoodEventAdapter", "Image URL fetched for " + moodEvent.getImgPath() + ": " + uri.toString());
                        if (currentImgPath.equals(holder.moodImage.getTag())) {
                        Glide.with(getContext())
                                .load(uri)
                                .into(holder.moodImage);
                        holder.moodImage.setVisibility(View.VISIBLE);} // Explicitly set visible on success

                    })

                    .addOnFailureListener(e -> {
                        Log.e("MoodEventAdapter", "Failed to fetch image URL for " + moodEvent.getImgPath() + ": " + e.getMessage());
                        holder.moodImage.setVisibility(View.GONE); // Hide on failure
                    });

        } else {
            Log.d("MoodEventAdapter", "No imgPath for mood event at position " + position);
            Glide.with(getContext()).clear(holder.moodImage); // Clear image if no path
            holder.moodImage.setVisibility(View.GONE); // Hide if no image
        }

        return convertView;
    }


    /**
     * Converts latitude and longitude to an address string.
     *
     * @param latitude The latitude of the location.
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
