package com.example.ctrlaltelite;

import static android.view.View.INVISIBLE;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ctrlaltelite.MainActivity;

import android.Manifest;
import android.content.Intent;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Calendar;

/**
 * Code for when viewing another user's profile
 */
public class OtherUserProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView displayNameText, usernameText, text_followers_count, text_following_count;
    private ListView moodListView;
    private MoodEventAdapter moodAdapter;
    private List<MoodEvent> moodEvents = new ArrayList<>();
    private User currentUser;
    private User searchedUser;

    /**
     * Construcutor
     * @param currentUser the user using the app
     * @param searchedUser the user who's profile is being viewed
     */
    public OtherUserProfileFragment(User currentUser, User searchedUser) {
        this.currentUser = currentUser;
        this.searchedUser = searchedUser;
    }

    /**
     * Getting an instance of the db
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    /*
     * Portions of this code were developed with guidance from OpenAI's ChatGPT.
     * ChatGPT was used to help debug issues, improve code reliability,.
     *
     * Assistance provided as of March 2025.
     */

    /**
     * Inflating the UI for viewing another user's profile
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_user_profile, container, false);

        final int[] state = {0};

        displayNameText = view.findViewById(R.id.display_name);
        usernameText = view.findViewById(R.id.username);
        moodListView = view.findViewById(R.id.mood_list);
        text_followers_count = view.findViewById(R.id.text_followers_count);
        Button chat = view.findViewById(R.id.chat);
        text_following_count = view.findViewById(R.id.text_following_count);

        moodAdapter = new MoodEventAdapter(requireContext(), moodEvents);
        moodListView.setAdapter(moodAdapter);

//        // Find the chat button
//        ImageButton chatButton = view.findViewById(R.id.chat_button);
//
//        // Set a click listener for the chat button
//        chatButton.setOnClickListener(v -> {
//            // Open the ChatActivity when the chat icon is clicked
//            Intent intent = new Intent(getActivity(), ChatActivity.class);
//
//            // Pass the clicked user's username or other data
//            intent.putExtra("username", selectedUser.getUsername());
//
//            // Optionally, pass other user details as well
//            intent.putExtra("user_email", selectedUser.getEmail()); // For example
//
//            // Start the ChatActivity
//            startActivity(intent);
//        });

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
                    chat.setVisibility(INVISIBLE);
                }

                else {

                    requestButton.setText("Loading");
                    Task<Boolean> checkingIfUserAlreadyAccepted = HasUserAlreadyAccepted(currentUser.getUsername(), searchedUser.getUsername());
                    final boolean[] hasOtherUserAlreadyAccepted = new boolean[1];
                    /**
                     * While the Task<Boolean> has been completed that determines if a user has already accepted or requested, we do our functionality of the follow
                                requesting system here.
                     */
                    checkingIfUserAlreadyAccepted.addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            hasOtherUserAlreadyAccepted[0] = task.getResult();

                            if (!hasOtherUserAlreadyAccepted[0]) {

                                Task<Boolean> checkingIfUserAlreadyRequested = HasUserAlreadyRequested(currentUser.getUsername(), searchedUser.getUsername());
                                final boolean[] hasUserAlreadyRequested = new boolean[1];
                                checkingIfUserAlreadyRequested.addOnCompleteListener(task1 -> {

                                    if (task1.isSuccessful()) {
                                        hasUserAlreadyRequested[0] = task1.getResult();

                                        if (!hasUserAlreadyRequested[0]) {

                                            requestButton.setText("Follow");

                                            /**
                                             * Functionality for when the user presses the request button
                                             */
                                            requestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {

                                                    if (state[0] == 0) {
                                                        FollowRequest newFollowRequest = new FollowRequest(currentUser.getUsername(), searchedUser.getUsername(), currentUser.getDisplayName(), searchedUser.getDisplayName(), "Pending");
                                                        saveToFirestore(newFollowRequest);
                                                        requestButton.setText("Requested");
                                                        Toast.makeText(getContext(), "Successfully requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                        state[0] = 1;
                                                    }
                                                    else {
                                                        Toast.makeText(getContext(), "You have already requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                        else {
                                            requestButton.setText("Requested");
                                            /**
                                             * Functionality for when the user has requested to follow a user they already requested to follow
                                             */
                                            requestButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Toast.makeText(getContext(), "You have already requested to follow " + searchedUser.getDisplayName(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else {
                                requestButton.setText("Following");
                            }
                        }
                    });
                }
            }
            else {
                Toast.makeText(getContext(), "No user selected", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getContext(), "No user data provided", Toast.LENGTH_SHORT).show();
        }

        // --- FOLLOWERS COUNT ---
        db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", searchedUser.getUsername())
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    // [GPT FIX] Fixed crash: setText expects a String, not an int resource ID
                    text_followers_count.setText(String.valueOf(count));

                });

// --- FOLLOWING COUNT ---
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", searchedUser.getUsername())
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    // [GPT FIX] Fixed crash: setText expects a String, not an int resource ID
                    text_following_count.setText(String.valueOf(count));

                });

        //Set a click listener for the chat button
        chat.setOnClickListener(v -> {
            // Open the ChatActivity when the chat icon is clicked
            Intent intent = new Intent(getActivity(), ChatActivity.class);

            // Pass the clicked user's username or other data
            intent.putExtra("displayName", searchedUser.getDisplayName());
            intent.putExtra("username", searchedUser.getUsername());

//            AndroidUtil.passUserAsIntent(intent, model);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            getContext().startActivity(intent);

        });

        return view;
    }

    /**
     * Fetching mood events for a user
     * @param username - username of the user we want to retrieve mood events from
     */
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

    /**
     * Checking to see if a follow request has already been made
     * @param followerUsername - user name of the person requesting
     * @param followingUsername - user name of the person who is being requested to
     * @return a Task<Boolean> which will be handled in the follow requesting functionality
     */
    public Task<Boolean> HasUserAlreadyRequested(String followerUsername, String followingUsername) {
        Log.d("OtherUserProfileFragment", "Checking to see if a request has already been made to " + followingUsername + " from " + followerUsername);

        return db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", followingUsername)
                .whereEqualTo("Requester's Username", followerUsername)
                .whereEqualTo("Status", "Pending")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty();
                    } else {

                        // Handle any specific errors (shouldn't happen though)
                        Toast.makeText(getContext(), "Error obtaining desired document", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
    }

    /**
     * Checking to see if a user is following another user
     * @param followerUsername username of user who is doing the following
     * @param followingUsername username of user who is being followed
     * @return a Task<Boolean> which will be handled in the follow requesting functionality
     */
    private Task<Boolean> HasUserAlreadyAccepted(String followerUsername, String followingUsername) {
        Log.d("OtherUserProfileFragment", "Checking to see if a request to " + followingUsername + " from " + followerUsername + " has been accepted");

        return db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", followingUsername)
                .whereEqualTo("Requester's Username", followerUsername)
                .whereEqualTo("Status", "Accepted")
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return !task.getResult().isEmpty();
                    } else {
                        // Handle any specific errors (should not happen though)
                        Toast.makeText(getContext(), "Error obtaining desired document", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
    }

    /**
     * Saving a follow request to the firestore db
     * @param followRequest - the follow request we want to save
     */
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