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
        timestampText.setText(moodEvent.getTimestamp());

        if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
            StorageReference imageRef = storageRef.child(moodEvent.getImgPath());
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(getContext()).load(uri).into(moodImage);
            }).addOnFailureListener(e -> {
                moodImage.setImageResource(android.R.drawable.ic_menu_gallery);
            });
        } else {
            moodImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        return convertView;
    }
}
