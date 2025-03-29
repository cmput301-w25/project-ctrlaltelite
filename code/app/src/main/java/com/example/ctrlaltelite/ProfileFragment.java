package com.example.ctrlaltelite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * The {@code ProfileFragment} displays the user's profile information.
 */
public class ProfileFragment extends AddFragment {

    private TextView usernameTextView, displayNameTextView, emailTextView, phoneTextView;
    private Button logoutButton;
    private FirebaseFirestore db;

    private String displayName;
    private String username;  // 🔹 Store username for Firestore query

    /**
     * Called when the fragment is first created.
     *
     * @param savedInstanceState The saved instance state, if available.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI elements
        usernameTextView = view.findViewById(R.id.text_username);
        displayNameTextView = view.findViewById(R.id.text_display_name);
        emailTextView = view.findViewById(R.id.text_email);
        phoneTextView = view.findViewById(R.id.text_phone);
        logoutButton = view.findViewById(R.id.button_logout);

        // Get username from fragment arguments
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }

        if (username == null || username.isEmpty()) {
            Toast.makeText(getContext(), "Error: No username found!", Toast.LENGTH_SHORT).show();
            return view;
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String displayName = sharedPreferences.getString("display_name", "");



        Log.d("ProfileFragment", "Fetching user data for username: " + username);

        // Fetch user details from Firestore using "username"
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        String email = document.getString("email");
                        String mobile = document.getString("mobile");

                        Log.d("ProfileFragment", "User found: " + username + " - " + email + " - " + mobile);

                        usernameTextView.setText("@"+ username);
                        displayNameTextView.setText(displayName);
                        emailTextView.setText(email != null ? email : "N/A");
                        phoneTextView.setText(mobile != null ? mobile : "N/A");
                    } else {
                        Toast.makeText(getContext(), "User details not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Firestore query failed: ", e);
                    Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });

        // Logout Button Click
        logoutButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to Login Screen
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
        });

        Button buttonFollowers = view.findViewById(R.id.button_followers);
        Button buttonFollowing = view.findViewById(R.id.button_following);

// --- FOLLOWERS COUNT ---
        db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    buttonFollowers.setText("Followers (" + count + ")");
                });

// --- FOLLOWING COUNT ---
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    buttonFollowing.setText("Following (" + count + ")");
                });

// --- Button Click Actions ---
        buttonFollowers.setOnClickListener(v -> {
            FollowersCountFragment fragment = new FollowersCountFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", username);  // Use the `username` you already fetched
            fragment.setArguments(bundle);

            ((MainActivity) getActivity()).fragmentRepl(fragment);
        });

        buttonFollowing.setOnClickListener(v -> {
            FollowingCountFragment fragment = new FollowingCountFragment();
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            fragment.setArguments(bundle);
            ((MainActivity) getActivity()).fragmentRepl(fragment);
        });

        return view;
    }
}
