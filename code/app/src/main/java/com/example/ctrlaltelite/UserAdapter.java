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

import kotlin.contracts.Returns;

/**
 * The type User adapter.
 */
public class UserAdapter extends ArrayAdapter<User> {

    /**
     * Instantiates a new User adapter.
     *
     * @param context the context
     * @param users   the users
     */
    public UserAdapter(@NonNull Context context, @NonNull List<User> users) {
        super(context, 0, users);
    }

    /** Returns a view of a user item at the specified position in the list.
     * @param position    The position of the item within the adapter's data set
     * @param convertView The recycled view to populate, or null if not available
     * @param parent      The parent ViewGroup that this view will be attached to
     * @return The populated view for the user item */
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