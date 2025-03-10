package com.example.ctrlaltelite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

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
        ImageView moodImage = convertView.findViewById(R.id.mood_image);

        moodText.setText(moodEvent.getEmotionalState());
        reasonText.setText(moodEvent.getReason() != null ? moodEvent.getReason() : "");
        triggerText.setText(moodEvent.getTrigger() != null ? moodEvent.getTrigger() : "");
        socialSituationText.setText(moodEvent.getSocialSituation() != null ? moodEvent.getSocialSituation() : "");
        timestampText.setText(moodEvent.getFormattedTimestamp());

        moodText.setTextColor(getColorForMood(moodEvent.getEmotionalState()));


        if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
            StorageReference imageRef = storageRef.child(moodEvent.getImgPath());
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(getContext()).load(uri).into(moodImage);
            }).addOnFailureListener(e -> {
                moodImage.setVisibility(View.GONE);
            });
        } else {
            moodImage.setVisibility(View.GONE);
        }

        return convertView;
    }
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