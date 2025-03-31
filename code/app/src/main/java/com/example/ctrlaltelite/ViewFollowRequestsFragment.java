package com.example.ctrlaltelite;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Code for the functionality when viewing follow requests
 */
public class ViewFollowRequestsFragment extends Fragment {
    private FirebaseFirestore db;
    private FollowRequestAdapter followRequestAdapter;
    private List<FollowRequest> followRequestList = new ArrayList<>();

    private String username;

    /**
     * Constructor
     * @param username - username of user who is viewing their follow requests
     */
    public ViewFollowRequestsFragment(String username) {
        this.username = username;
    }

    /**
     * Setting up the db
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Inflating the UI for viewing follow requests
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_follow_requests, container, false);

        ImageButton buttonDrawerToggle = view.findViewById(R.id.buttonDrawerToggle);

        ListView followRequestListView = view.findViewById(R.id.follow_request_list);
        followRequestAdapter = new FollowRequestAdapter(requireContext(), followRequestList);
        followRequestListView.setAdapter(followRequestAdapter);
        fetchFollowRequests(username);

        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();

        if (buttonDrawerToggle != null && mainActivity != null) {
            buttonDrawerToggle.setOnClickListener(v -> {
                mainActivity.openDrawer();
            });
        }

        return view;
    }

    /**
     * Fetching all the follow requests of a user
     * @param username - username of user who's follow requests we will retrieve
     */
    private void fetchFollowRequests(String username) {
        Log.d("OtherUserProfileFragment", "Fetching follow requests for username: " + username);
        db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", username)
                .whereEqualTo("Status", "Pending")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followRequestList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String followerUserName = document.getString("Requester's Username");
                            String followingUserName = document.getString("Requestee's Username");
                            String followerDisplayName = document.getString("Requester's Display Name");
                            String followingDisplayName = document.getString("Requestee's Display Name");
                            String status = document.getString("Status");
                            String docId = document.getId();
                            FollowRequest followRequest = new FollowRequest(followerUserName, followingUserName, followerDisplayName,followingDisplayName, status);
                            followRequest.setDocumentId(docId);
                            followRequestList.add(followRequest);
                            
                        }
                        followRequestAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error loading follow requests for username: " + username, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
