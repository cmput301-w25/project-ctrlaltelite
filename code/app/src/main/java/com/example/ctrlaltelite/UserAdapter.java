package com.example.ctrlaltelite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {

    public UserAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, 0, users);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_item, parent, false);
        }

        User user = getItem(position);

        ImageView profilePhoto = convertView.findViewById(R.id.profile_photo);
        TextView displayNameText = convertView.findViewById(R.id.display_name);
        TextView usernameText = convertView.findViewById(R.id.username);

        if (user != null) {
            displayNameText.setText(user.getDisplayName()); // username for now
            usernameText.setText("@" + user.getUsername());

            if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
                Glide.with(getContext())
                        .load(user.getProfilePhotoUrl())
                        .placeholder(R.drawable.profile)
                        .into(profilePhoto);
            } else {
                profilePhoto.setImageResource(R.drawable.profile);
            }
        }

        return convertView;
    }
}