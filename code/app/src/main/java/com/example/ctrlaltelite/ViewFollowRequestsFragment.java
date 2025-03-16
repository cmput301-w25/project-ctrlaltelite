package com.example.ctrlaltelite;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewFollowRequestsFragment extends Fragment {
    private FirebaseFirestore db;
    private FollowRequestAdapter followRequestAdapter;
    private List<FollowRequest> followRequestList = new ArrayList<>();

    private String username;

    public ViewFollowRequestsFragment(String username) {
        this.username = username;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_follow_requests, container, false);
        ListView followRequestListView = view.findViewById(R.id.follow_request_list);
        followRequestAdapter = new FollowRequestAdapter(requireContext(), followRequestList);
        followRequestListView.setAdapter(followRequestAdapter);
        fetchFollowRequests(username);
        return view;
    }

    private void fetchFollowRequests(String username) {
        Log.d("OtherUserProfileFragment", "Fetching follow requests for username: " + username);
        db.collection("FollowRequests")
                .whereEqualTo("Following", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followRequestList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String followerUserName = document.getString("Follower");
                            String followingUserName = document.getString("Following");
                            String status = document.getString("Status");
                            String docId = document.getId();
                            FollowRequest followRequest = new FollowRequest(followerUserName, followingUserName, status);
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
