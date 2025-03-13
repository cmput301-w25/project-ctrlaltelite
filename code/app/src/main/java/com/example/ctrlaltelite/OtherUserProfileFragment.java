package com.example.ctrlaltelite;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OtherUserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView displayNameText, usernameText;
    private ListView moodListView;
    private MoodEventAdapter moodAdapter;
    private List<MoodEvent> moodEvents = new ArrayList<>();

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
            } else {
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
}