package com.example.ctrlaltelite;

import static android.view.View.INVISIBLE;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ctrlaltelite.MainActivity;

import android.Manifest;
import android.content.pm.PackageManager;
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
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Calendar;

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

                if (usernameText.getText().equals("@" + currentUser.getUsername())) {
                    requestButton.setVisibility(INVISIBLE);
                } else {
                    requestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Task<Boolean> checkingIfUserAlreadyRequested = HasUserAlreadyRequested(currentUser.getUsername(), searchedUser.getUsername());
                            final boolean[] hasUserAlreadyRequested = new boolean[1];

                            checkingIfUserAlreadyRequested.addOnCompleteListener(task -> {
                               if (task.isSuccessful()) {
                                   hasUserAlreadyRequested[0] = task.getResult();

                                   if (!hasUserAlreadyRequested[0]) {
                                       FollowRequest newFollowRequest = new FollowRequest(currentUser.getUsername(), searchedUser.getUsername(), currentUser.getDisplayName(), searchedUser.getDisplayName(), "Pending");
                                       saveToFirestore(newFollowRequest);
                                       requestButton.setText("Requested");
                                       Toast.makeText(getContext(), "Successfully requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                   } else {
                                       Toast.makeText(getContext(), "You have already requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                   }
                               }
                            });
                        }
                    });
                }
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

    private Task<Boolean> HasUserAlreadyRequested(String followerUsername, String followingUsername) {
        Log.d("OtherUserProfileFragment", "Checking to see if a request has already been made to " + followingUsername + " from " + followerUsername);

        return db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", followingUsername)
                .whereEqualTo("Requester's Username", followerUsername)
                .whereEqualTo("Status", "Pending")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        // Check if any documents match the query
                        return !task.getResult().isEmpty();
                    } else {
                        // Handle errors
                        Toast.makeText(getContext(), "Error obtaining desired document", Toast.LENGTH_SHORT).show();
                        return false; // Assume no request exists in case of error
                    }
                });
    }
    protected void saveToFirestore(FollowRequest followRequest) {
        String currentUserUsername = followRequest.getRequesterUserName();
        String searchedUserUsername = followRequest.getRequestedUserName();
        String currentUserDisplayName = followRequest.getRequesterDisplayName();
        String searchedUserDisplayName = followRequest.getRequestedDisplayName();
        String status = followRequest.getStatus();

        Map<String, Object> followRequestToBeAdded = new HashMap<>();
        followRequestToBeAdded.put("Requester's Username", currentUserUsername);
        followRequestToBeAdded.put("Requestee's Username", searchedUserUsername);
        followRequestToBeAdded.put("Requester's Display Name", currentUserDisplayName);
        followRequestToBeAdded.put("Requestee's Display Name", searchedUserDisplayName);
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
}