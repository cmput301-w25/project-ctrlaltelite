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

public class HomeFragment extends Fragment {

    private ListView listView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> moodEvents;
    private FirebaseFirestore db;
    private String username;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            username = args.getString("username");
            Log.d("MoodHistoryFragment", "Fetching mood events for Username: " + username);
        }

        if (username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize ListView
        listView = view.findViewById(R.id.mood_list);
        moodEvents = new ArrayList<>();
        adapter = new MoodEventAdapter(requireContext(), moodEvents);
        listView.setAdapter(adapter);

        // Fetch mood events for the specific user
        fetchMoodEvents();
        //fetchMoodEvents();

        return view;
    }

    private void fetchMoodEvents() {
        db.collection("Mood Events")
                .whereEqualTo("Username", username) // Filter by username
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodEvents.clear(); // Clear existing data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvents.add(moodEvent);
                        }
                        adapter.notifyDataSetChanged(); // Update the ListView
                        Log.d("HomeFragment", "Mood events fetched: " + moodEvents.size());
                    } else {
                        Log.w("HomeFragment", "Error fetching mood events", task.getException());
                        Toast.makeText(getContext(), "Error loading mood events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}