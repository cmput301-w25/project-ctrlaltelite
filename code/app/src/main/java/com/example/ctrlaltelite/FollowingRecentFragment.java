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

import java.util.List;

import javax.annotation.Nullable;

public class FollowingRecentFragment extends Fragment {
    private FirebaseFirestore db;
    private MoodEventAdapter followingRecentAdapter;
    private List<MoodEvent> allMoodEvents;
    private List<MoodEvent> recentMoodEvents;
    private List<String> followedUsernames;

    private String username;

    private FollowingRecentFragment(String username) {
        this.username = username;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstance) {
        View view = inflater.inflate(R.layout.fragment_following_recent, container, false);

        ListView followingRecentListView = view.findViewById(R.id.mood_list);
        followingRecentAdapter = new MoodEventAdapter(requireContext(), recentMoodEvents);
        followingRecentListView.setAdapter(followingRecentAdapter);
        fetchFollowedUsers(username);
        fetchAllMoodEvents(username);

        if (allMoodEvents.size() > 3) {
            for (int i = 0; i < 3; i++) {
                recentMoodEvents.add(allMoodEvents.get(i));
            }
        }
        else {
            for (int i = 0; i < allMoodEvents.size(); i++) {
                recentMoodEvents.add(allMoodEvents.get(i));
            }
        }
        followingRecentAdapter.notifyDataSetChanged();

        return view;
    }

    public void fetchFollowedUsers(String username) {
        Log.d("FollowingRecentFragment", "Obtaining all followed user");
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followedUsernames.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String followingUserName = document.getString("Requestee's Username");
                            followedUsernames.add(followingUserName);
                        }
                    }
                    else {
                        Toast.makeText(getContext(), "Error loading user requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void fetchAllMoodEvents(String username) {
        Log.d("FollowingRecentFragment", "Fetching all mood events from all followed user, then obtaining the 3 most recent ones");
        allMoodEvents.clear();
        for (int i = 0; i < followedUsernames.size(); i++) {
            db.collection("Mood Events")
                    .whereEqualTo("username", followedUsernames.get(i))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: task.getResult()) {
                                MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                moodEvent.setDocumentId(document.getId()); // Ensure docId is set
                                allMoodEvents.add(moodEvent);
                            }
                        }
                    });
        }
        toggleSort();
    }

    public void toggleSort() {
        if (allMoodEvents == null || allMoodEvents.isEmpty()) return;
        allMoodEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
    }

}
