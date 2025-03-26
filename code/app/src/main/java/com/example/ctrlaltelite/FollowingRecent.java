package com.example.ctrlaltelite;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class FollowingRecent extends Fragment {
    private FirebaseFirestore db;
    private MoodEventAdapter followingRecentAdapter;
    private List<MoodEvent> allMoodEvents = new ArrayList<>();  // Initialized to empty list
    private List<MoodEvent> recentMoodEvents = new ArrayList<>();  // Initialized to empty list
    private List<String> followedUsernames = new ArrayList<>();

    private String username;

    private String Username;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    private int fetchCounter = 0; // Counter to track when all users' mood events are fetched.


    /**
     * Default constructor.
     */
    public FollowingRecent() {
        // Required empty public constructor
    }


    /**
     * Called when the fragment is created.
     *
     * @param savedInstanceState A saved instance state if the fragment is re-initialized.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstance) {
        View view = inflater.inflate(R.layout.fragment_following_recent, container, false);
        db = FirebaseFirestore.getInstance();

        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
        }
        if (Username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }


        // Initialize ListView and Adapter
        ListView followingRecentListView = view.findViewById(R.id.mood_list);
        followingRecentAdapter = new MoodEventAdapter(requireContext(), recentMoodEvents);
        followingRecentListView.setAdapter(followingRecentAdapter);

        // Fetch followed users and mood events
        fetchFollowedUsers(Username);
        fetchAllMoodEvents(Username);


        return view;
    }


    public void fetchFollowedUsers(String username) {
        Log.d("FollowingRecentFragment", "Obtaining all followed user");
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", Username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followedUsernames.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String followingUserName = document.getString("Requestee's Username");
                            followedUsernames.add(followingUserName);
                        }
                        // Once followed users are fetched, fetch their mood events
                        fetchAllMoodEvents(Username);
                    } else {
                        Toast.makeText(getContext(), "Error loading user requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void fetchAllMoodEvents(String Username) {
        Log.d("FollowingRecentFragment", "Fetching all mood events from all followed user");
        allMoodEvents.clear();
        fetchCounter = 0; // Reset counter before fetching

        for (String followedUser : followedUsernames) {
            db.collection("Mood Events")
                    .whereEqualTo("username", followedUser)
                    .whereEqualTo("public", true)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                moodEvent.setDocumentId(document.getId());  // Ensure docId is set
                                if (!isDuplicateMoodEvent(moodEvent)) {
                                    allMoodEvents.add(moodEvent);
                                }
                            }

                            fetchCounter++; // Increment counter after fetching mood events for one user

                            if (fetchCounter == followedUsernames.size()) {
                                toggleSort();
                                // All users' mood events fetched, now update the recent events
                                updateRecentMoodEvents();
                            }
                        }
                    });
        }

    }


    // This function was generated with the assistance of OpenAI's ChatGPT (2025).

    private boolean isDuplicateMoodEvent(MoodEvent moodEvent) {
        // Check if the moodEvent already exists in the list based on its document ID
        for (MoodEvent existingEvent : allMoodEvents) {
            if (existingEvent.getDocumentId().equals(moodEvent.getDocumentId())) {
                return true; // Duplicate found
            }
        }
        return false; // No duplicate
    }


    private void updateRecentMoodEvents() {
        recentMoodEvents.clear();
        if (allMoodEvents.size() > 3) {
            for (int i = 0; i < 3; i++) {
                recentMoodEvents.add(allMoodEvents.get(i));
            }
        } else {
            recentMoodEvents.addAll(allMoodEvents);
        }
        // Notify the adapter that the data has changed
        followingRecentAdapter.notifyDataSetChanged();
    }


        public void toggleSort() {
        if (allMoodEvents == null || allMoodEvents.isEmpty()) return;
        allMoodEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
    }

}