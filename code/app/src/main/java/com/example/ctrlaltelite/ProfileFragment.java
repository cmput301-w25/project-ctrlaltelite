package com.example.ctrlaltelite;

import android.content.Intent;
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

    private TextView usernameTextView, emailTextView, phoneTextView;
    private Button logoutButton;
    private FirebaseFirestore db;
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



        Log.d("ProfileFragment", "Fetching user data for username: " + username);

        // 🔥 Fetch user details from Firestore using "username"
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        String email = document.getString("email");
                        String mobile = document.getString("mobile");

                        Log.d("ProfileFragment", "User found: " + username + " - " + email + " - " + mobile);

                        usernameTextView.setText(username);
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

        // 🔥 Logout Button Click
        logoutButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to Login Screen
            Intent intent = new Intent(getActivity(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
        });

        return view;
    }
}
