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

public class FollowRequestAdapter extends ArrayAdapter<FollowRequest> {
    public FollowRequestAdapter(@NonNull Context context, @NonNull List<FollowRequest> followRequests) {
        super(context, 0, followRequests);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.follow_request_item, parent, false);
        }

        FollowRequest followRequest = getItem(position);
        TextView followerDisplayName = convertView.findViewById(R.id.follower_display_name);
        TextView followingDisplayName = convertView.findViewById(R.id.following_display_name);
        TextView status = convertView.findViewById(R.id.status);

        if (followRequest != null) {
            followerDisplayName.setText(followRequest.getRequester().getDisplayName());
            followingDisplayName.setText(followRequest.getRequestedUser().getDisplayName());
            status.setText(followRequest.getStatus());
        }
        return convertView;
    }
}
