package com.example.ctrlaltelite;

import static android.widget.Toast.makeText;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for follow requests
 */
public class FollowRequestAdapter extends ArrayAdapter<FollowRequest> {

    private FirebaseFirestore db;
    List<FollowRequest> followRequestsList = new ArrayList<>();

    /**
     * Constructor
     * @param context
     * @param followRequests - list of follow requests
     */
    public FollowRequestAdapter(@NonNull Context context, @NonNull List<FollowRequest> followRequests) {
        super(context, 0, followRequests);
        this.followRequestsList = followRequests;
    }

    /**
     * Setting up the adapter
     * @param position
     * @param convertView
     * @param parent
     * @return view with adapater inflating the follow request item XML
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        db = FirebaseFirestore.getInstance();

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.follow_request_item, parent, false);
        }

        FollowRequest followRequest = getItem(position);
        TextView followingUsernameName = convertView.findViewById(R.id.following_username);
        TextView followingDisplayName = convertView.findViewById(R.id.following_display_name);

        if (followRequest != null) {
            followingUsernameName.setText("@" + followRequest.getRequesterUserName());
            followingDisplayName.setText(followRequest.getRequesterDisplayName());
        }

        Button acceptButton = convertView.findViewById(R.id.accept_button);
        Button rejectButton = convertView.findViewById(R.id.reject_button);

        acceptButton.setOnClickListener(new View.OnClickListener() {

            /**
             * When user clicks the accept button
             * @param v
             */
            @Override
            public void onClick(View v) {
                onAcceptButton(followRequest);
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            /**
             *  when user presses the reject button
             * @param v
             */
            @Override
            public void onClick(View v) {
                onRejectButton(followRequest);
            }
        });

        return convertView;
    }

    /**
     * Functionality when accepting requests
     * @param followRequest
     */
    private void onAcceptButton(FollowRequest followRequest) {
        if (!followRequestsList.isEmpty()) {
            followRequestsList.remove(followRequest);
            notifyDataSetChanged();
            updateFollowRequestInFirestore(followRequest, 0);
            Toast.makeText(getContext(), "You have accepted " + followRequest.getRequesterUserName() + "'s request", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Functionality when rejecting requests
     * @param followRequest
     */
    private void onRejectButton(FollowRequest followRequest) {
        if (!followRequestsList.isEmpty()) {
            followRequestsList.remove(followRequest);
            notifyDataSetChanged();
            updateFollowRequestInFirestore(followRequest, 1);
            Toast.makeText(getContext(), "You have rejected " + followRequest.getRequesterUserName() + "'s request", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Making updates to firestore after a status update to a follow request
     * @param followRequest - the follow request whose status has been changed
     * @param rejectOrAccept - the two possible cases
     */
    private void updateFollowRequestInFirestore(FollowRequest followRequest, int rejectOrAccept) {
        String docId = followRequest.getDocumentId();
        if (docId == null) {
            Log.e("ViewFollowRequestsFragment", "Document ID is null for Follow Reqeust");
            Toast.makeText(getContext(), "Cannot update follow request", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ViewFollowRequestsFragment", "Attempting to update Follow Request with ID " + docId);

        if (rejectOrAccept == 0) {
            db.collection("FollowRequests")
                .document(docId)
                .update("Status", "Accepted");
        }

        if (rejectOrAccept == 1) {
            db.collection("FollowRequests")
                    .document(docId)
                    .update("Status", "Rejected");
        }
        notifyDataSetChanged();
    }

}
