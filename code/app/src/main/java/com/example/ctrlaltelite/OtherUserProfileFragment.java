package com.example.ctrlaltelite;

import static android.view.View.INVISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherUserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView displayNameText, usernameText;
    private ListView moodListView;
    private MoodEventAdapter moodAdapter;
    private List<MoodEvent> moodEvents = new ArrayList<>();
    private User currentUser;
    private User searchedUser;

    private List<FollowRequest> followRequestList = new ArrayList<>();

    private User desiredUser;

    public OtherUserProfileFragment(User currentUser, User searchedUser) {
        this.currentUser = currentUser;
        this.searchedUser = searchedUser;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        displayNameText = view.findViewById(R.id.display_name);
        usernameText = view.findViewById(R.id.username);
        moodListView = view.findViewById(R.id.mood_list);

        moodAdapter = new MoodEventAdapter(requireContext(), moodEvents);
        moodListView.setAdapter(moodAdapter);

        Bundle args = getArguments();
        if (args != null) {
            String displayName = args.getString("displayName");
            String clickedUsername = args.getString("clickedUsername");
            if (clickedUsername != null) {
                displayNameText.setText(displayName != null ? displayName : clickedUsername);
                usernameText.setText("@" + clickedUsername);
                fetchMoodEvents(clickedUsername);

                Button requestButton = view.findViewById(R.id.follow_button);

                if(usernameText.getText().equals("@"+ currentUser.getUsername())) {
                    requestButton.setVisibility(INVISIBLE);
                }

                else {
                    requestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fetchFollowRequests(searchedUser.getUsername());
                            boolean alreadyRequested = false;

                            for(int i = 0; i < followRequestList.size(); i++) {
                                if (followRequestList.get(i).getRequester().getUsername().equals(currentUser.getUsername())) {
                                    alreadyRequested = true;
                                }
                            }

                            if (!alreadyRequested) {
                                FollowRequest newFollowRequest = new FollowRequest(currentUser, searchedUser, "Pending");
                                saveToFirestore(newFollowRequest);
                                requestButton.setText("Requested");
                                Toast.makeText(getContext(), "Successfully requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                            } else {
                                requestButton.setText("Requested");
                                Toast.makeText(getContext(), "You have already requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            else {
                Toast.makeText(getContext(), "No user selected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No user data provided", Toast.LENGTH_SHORT).show();
        }
            return view;

    }

    private void fetchMoodEvents(String username) {
        Log.d("OtherUserProfileFragment", "Fetching mood events for username: " + username);
        db.collection("Mood Events")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodEvents.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            String docId = document.getId();
                            moodEvent.setDocumentId(docId);
                            moodEvents.add(moodEvent);
                        }
                        moodEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
                        moodAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Error loading mood history", Toast.LENGTH_SHORT).show();
                    }
                });
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
                            FollowRequest followRequest = new FollowRequest(getUser(followerUserName), getUser(followingUserName), status);
                            followRequest.setDocumentId(docId);
                            followRequestList.add(followRequest);
                        }
                    } else {
                        Toast.makeText(getContext(), "Error loading follow requests for username: " + username, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void saveToFirestore(FollowRequest followRequest) {
        String currentUserUsername = followRequest.getRequester().getUsername();
        String searchedUserUsername = followRequest.getRequestedUser().getUsername();
        String status = followRequest.getStatus();

        Map<String, Object> followRequestToBeAdded = new HashMap<>();
        followRequestToBeAdded.put("Follower", currentUserUsername);
        followRequestToBeAdded.put("Following", searchedUserUsername);
        followRequestToBeAdded.put("Status", status);

        db.collection("FollowRequests")
                .add(followRequestToBeAdded)
                .addOnSuccessListener(documentReference -> {
                    followRequest.setDocumentId(documentReference.getId());
                    Log.d("AddFragment", "Saved Follow Request with docId: " + followRequest.getDocumentId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error making follow request", Toast.LENGTH_SHORT).show();
                });
    }

    private User getUser(String username) {

        Log.d("OtherUserProfileFragment", "Obtaining the User object for username: " + username);
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        desiredUser = document.toObject(User.class);
                    }
                });

        if (desiredUser == null) {
            return null;
        }
        else {
            return desiredUser;
        }

    }
}