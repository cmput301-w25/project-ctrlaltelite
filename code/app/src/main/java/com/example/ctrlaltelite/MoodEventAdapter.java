package com.example.ctrlaltelite;

import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;


import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Comment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom Adapter for displaying mood events in a ListView.
 * This adapter binds data from a list of MoodEvent objects to the ListView items.
 * It handles setting text, loading images from Firebase Storage, and displaying geolocation information.
 */

public class MoodEventAdapter extends ArrayAdapter<MoodEvent> {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private boolean savingInProgress = false;

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
        db = FirebaseFirestore.getInstance();
    }


    /**
     * ViewHolder class to cache view references and improve performance by avoiding
     * repeated calls to findViewById.
     */
    // The ViewHolder is used to improve the performance of ListView.
    // It helps avoid repeatedly finding views during scrolling by holding references to the UI components
    static class ViewHolder {
        TextView displayName;
        TextView moodText;
        TextView reasonText;
        TextView socialSituationText;
        TextView timestampText;
        TextView geolocationText;
        ImageView moodImage;
        ImageButton commentButton;
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
            holder.displayName = convertView.findViewById(R.id.display_name);
            holder.moodText = convertView.findViewById(R.id.mood_text);
            holder.reasonText = convertView.findViewById(R.id.reason_text);
            holder.socialSituationText = convertView.findViewById(R.id.social_situation_text);
            holder.timestampText = convertView.findViewById(R.id.timestamp_text);
            holder.geolocationText = convertView.findViewById(R.id.geolocation);
            holder.moodImage = convertView.findViewById(R.id.mood_image);
            holder.commentButton = convertView.findViewById(R.id.comments_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MoodEvent moodEvent = getItem(position);
        if (moodEvent == null) return convertView;
        final String moodEventId = moodEvent.getDocumentId();


        // Bind data to views
        db.collection("users")
                .whereEqualTo("username", moodEvent.getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        User user = document.toObject(User.class);
                        // Retrieve the logged-in user's display name from SharedPreferences
                        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                        String currentUsername = sharedPreferences.getString("display_name", "");  // Default to empty string if not found // Get the display name of the logged-in user

                        String currentUserID = sharedPreferences.getString("user", "");
                        holder.displayName.setText(user.getDisplayName() + " is feeling");

                        holder.commentButton.setOnClickListener(v -> {
                            showCommentsDialog(moodEvent.getDocumentId(), currentUsername, currentUserID); // Pass currentUsername here
                        });
                    }
                });
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
                        /* On retrieval
                         * dangle */
                        if (currentImgPath.equals(holder.moodImage.getTag())) {
                        Glide.with(getContext())
                                .load(uri)
                                .into(holder.moodImage);
                        holder.moodImage.setVisibility(View.VISIBLE);} // Explicitly set visible on success

                    })
                    .addOnFailureListener(e -> {
                        /* On retrieval
                         * dangle */
                        holder.moodImage.setVisibility(View.GONE); // Hide on failure
                    });
        } else {
            Glide.with(getContext()).clear(holder.moodImage); // Clear image if no path
            holder.moodImage.setVisibility(View.GONE); // Hide if no image
        }

//        // Set OnClickListener for the comment button
//        holder.commentButton.setOnClickListener(v -> {
//            showCommentsDialog(moodEventId);
//        });

        return convertView;
    }


    private void showCommentsDialog(String moodEventId, String currentUsername, String currentUserID) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_comments, null);

        // Initialize RecyclerView for displaying comments
        RecyclerView recyclerView = dialogView.findViewById(R.id.comments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final CommentsAdapter commentsAdapter = new CommentsAdapter();

        recyclerView.setAdapter(commentsAdapter);

        // Get the comments from Firestore for the selected mood event
        CollectionReference commentsRef = db.collection("Mood Events").document(moodEventId).collection("comments");
        commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CommentData> commentDataList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Convert Firestore data to CommentData
                        CommentData comment = document.toObject(CommentData.class);
                        commentDataList.add(comment);
                    }

                    // Set the comments data to the adapter
                    commentsAdapter.setComments(commentDataList);  // Pass the list of CommentData
                })
                .addOnFailureListener(e -> {
                    // Handle failure if any error occurs while fetching comments
                });

        // Set up the comment input field and button for submitting a new comment
        EditText commentInput = dialogView.findViewById(R.id.comment_input);
        Button submitCommentButton = dialogView.findViewById(R.id.submit_comment_button);

        submitCommentButton.setOnClickListener(v -> {
            String newCommentText = commentInput.getText().toString().trim();
            if (!newCommentText.isEmpty() && !savingInProgress) {
                savingInProgress = true;
                submitCommentButton.setEnabled(false);

                // Create a new CommentData object
                CommentData newComment = new CommentData(newCommentText, currentUsername, currentUserID, Timestamp.now());

                // Add the comment to Firestore in the "comments" sub-collection
                commentsRef.add(newComment)
                        .addOnSuccessListener(documentReference -> {
                            savingInProgress = false;
                            submitCommentButton.setEnabled(true);

                            commentInput.setText(""); // Clear the input field
                            // Reload comments (this will call the onSuccess listener above)
                            commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)
                                    .get()
                                    .addOnSuccessListener(updatedSnapshots -> {
                                        List<CommentData> updatedComments = new ArrayList<>();
                                        for (QueryDocumentSnapshot doc : updatedSnapshots) {
                                            CommentData updatedComment = doc.toObject(CommentData.class);
                                            updatedComments.add(updatedComment);
                                        }
                                        commentsAdapter.setComments(updatedComments);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            savingInProgress = false;
                            submitCommentButton.setEnabled(true);
                            // Handle failure
                        });
            } else {
                Toast.makeText(getContext(), "Comments can not be empty", LENGTH_SHORT).show();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();
        dialog.show();
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
            case "😨 Fear": return 0xFF3F51B5;  // Indigo
            case "😳 Shame": return 0xFFFF8DAA; // pink
            default: return 0xFF616161; // Default Gray
        }
    }
}
