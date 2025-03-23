package com.example.ctrlaltelite;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code FollowingFragment} displays the list of users that the logged-in user is following.
 * This fragment will later be updated to fetch and show followed users' mood updates.
 */


public class FollowingFragment extends Fragment {

    private String Username;

    private GeoPoint updatedLocation;
    private boolean isSorted = false; // Tracks sorting state

    private ListView listView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> filteredEvents = new ArrayList<>();
    private List<String> followedUsers = new ArrayList<>();
    private FirebaseFirestore db;

    // Image picking variables
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Uri newImageRef; // Holds the selected image URI (no array needed now)
    private ImageView imagePreview; // To display the image in the dialog
    private static final int MAX_SIZE = 65536; // Max image size in bytes
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private List<MoodEvent> allMoodEvents = new ArrayList<>();
    private String moodFilter = "Mood"; // Default: no filter
    private boolean weekFilter = false; // Default: all time
    private String reasonFilter = "";   // Default: no search

    /**
     * Default constructor.
     */
    public FollowingFragment() {
        // Required empty public constructor
    }


    /**
     * Called when the fragment is created.
     *
     * @param savedInstanceState A saved instance state if the fragment is re-initialized.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    /**
     * Inflates the UI layout for the "Following" section.
     *
     * @param inflater           The LayoutInflater used to inflate views.
     * @param container          The parent view the fragment's UI should be attached to.
     * @param savedInstanceState A saved instance state if the fragment is re-initialized.
     * @return The View object containing the fragment UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_following, container, false);
        db = FirebaseFirestore.getInstance();
        ImageButton buttonDrawerToggle = view.findViewById(R.id.buttonDrawerToggle);


        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
        }
        if (Username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Initialize Spinner
        Spinner moodFilterSpinner = view.findViewById(R.id.mood_filter);
        CheckBox weekFilterCheckBox = view.findViewById(R.id.show_past_week);
        EditText reasonFilterEditText = view.findViewById(R.id.search_mood_reason);

        // Get mood options from resources
        List<String> moodFilterOptions = new ArrayList<>();
        moodFilterOptions.add("Mood");  // Default text only for the filter
        moodFilterOptions.addAll(Arrays.asList(getResources().getStringArray(R.array.mood_options)).subList(1, 7)); // Skip "Select Emotional State"
        CustomSpinnerAdapter moodAdapter = new CustomSpinnerAdapter(requireContext(), moodFilterOptions);
        moodFilterSpinner.setAdapter(moodAdapter);

        // Initialize ListView
        listView = view.findViewById(R.id.mood_list);
        adapter = new MoodEventAdapter(requireContext(), filteredEvents);
        listView.setAdapter(adapter);
        fetchMoodEvents(); //get list of mood events from those users which are followed

        //spinner for which mood all displayed mood events should have
        moodFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moodFilter = moodFilterOptions.get(position);
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Week filter
        weekFilterCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekFilter = isChecked;
            applyFilters();
        });

        // Reason filter
        reasonFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reasonFilter = s.toString().trim().toLowerCase();
                applyFilters(); // Apply filters immediately on text change
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });



        return view;
    }

    private void applyFilters() {
        if (allMoodEvents == null || allMoodEvents.isEmpty()) {
            filteredEvents.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        filteredEvents.clear();
        filteredEvents.addAll(allMoodEvents);

        if (!moodFilter.equals("Mood")) {
            List<MoodEvent> filteredList = filteredEvents.stream()
                    .filter(event -> {
                        String emotionalState = event.getEmotionalState();
                        return emotionalState.trim().equals(moodFilter.trim());
                    })
                    .collect(Collectors.toList());
            filteredEvents.clear();
            filteredEvents.addAll(filteredList);
        }

        if (weekFilter) {
            Calendar oneWeekAgo = Calendar.getInstance();
            oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7);
            long oneWeekAgoMillis = oneWeekAgo.getTimeInMillis();
            List<MoodEvent> filteredList = filteredEvents.stream()
                    .filter(event -> event.getTimestamp().toDate().getTime() >= oneWeekAgoMillis)
                    .collect(Collectors.toList());
            filteredEvents.clear();
            filteredEvents.addAll(filteredList);
        }

        if (!reasonFilter.isEmpty()) {
            List<MoodEvent> filteredList = filteredEvents.stream()
                    .filter(event -> Arrays.asList(event.getReason().toLowerCase().split("\\s+"))
                            .contains(reasonFilter))
                    .collect(Collectors.toList());
            filteredEvents.clear();
            filteredEvents.addAll(filteredList);
        }

        filteredEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
        adapter.notifyDataSetChanged(); // Notify adapter of changes to the existing list
    }

    public void toggleSort() {
        if (filteredEvents == null || filteredEvents.isEmpty()) return;
        filteredEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
        adapter.notifyDataSetChanged();
    }

    /**
     * Listens for real-time updates to the mood events associated with the current user in Firestore.
     * Automatically updates the local list and UI adapter whenever changes occur in the database.
     */
    public void fetchMoodEvents() {
        //Get all users who the current user follows
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", Username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followedUsers.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            followedUsers.add(document.getString("Requestee's Username")); //add followee
                        }

                        if (!followedUsers.isEmpty()) {
                            db.collection("Mood Events")
                                .whereIn("username", followedUsers)
                                .whereEqualTo("public", true)
                                .get() // Fetch data initially
                                .addOnCompleteListener(task2 -> {
                                    allMoodEvents.clear(); // Reset full list
                                    for (QueryDocumentSnapshot document : task2.getResult()) {
                                        MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                        moodEvent.setDocumentId(document.getId()); // Ensure docId is set
                                        allMoodEvents.add(moodEvent);
                                    }

                                    // Add all events initially from all followees
                                    filteredEvents.clear();
                                    filteredEvents.addAll(allMoodEvents);

                                    // Sort and Refresh UI
                                    toggleSort();
                                    adapter.notifyDataSetChanged();
                                });
                    }
                    }
                });

        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", Username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        followedUsers.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            followedUsers.add(document.getString("Requestee's Username")); //add followee
                        }

                        if (!followedUsers.isEmpty()) {
                        db.collection("Mood Events")
                                .whereIn("username", followedUsers)
                                .whereEqualTo("public", true)
                                .addSnapshotListener((value, error) -> {
                                    if (error != null) {
                                        return;
                                    }

                                    if (value != null) {
                                        allMoodEvents.clear(); // Reset full list
                                        for (QueryDocumentSnapshot document : value) {
                                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                                            moodEvent.setDocumentId(document.getId()); // Set docId again (to ensure updates)
                                            allMoodEvents.add(moodEvent);
                                        }

                                        // Update displayed list
                                        filteredEvents.clear();
                                        filteredEvents.addAll(allMoodEvents);

                                        // Sort and Refresh UI After Updating the List
                                        toggleSort();
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                    }
                });
    }
}