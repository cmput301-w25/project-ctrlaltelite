package com.example.ctrlaltelite;

import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;


import java.sql.Time;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Import the MoodEvent class
import com.example.ctrlaltelite.MoodEvent;

public class AddFragment extends Fragment {

    private Spinner dropdownMood;
    private Spinner socialSituation;
    private EditText editReason, editTrigger;
    private Switch switchLocation;
    private Button buttonSave, buttonCancel;
    private  String username;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();  // Firebase Authentication instance


        if (getArguments() != null) {
            username = getArguments().getString("username");

        }


        // Initialize UI elements
        dropdownMood = view.findViewById(R.id.dropdown_mood);
        socialSituation = view.findViewById(R.id.social_situation_spinner);
        editReason = view.findViewById(R.id.edit_reason);
        editTrigger = view.findViewById(R.id.edit_trigger);
        switchLocation = view.findViewById(R.id.switch_location);
        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);

        setupDropdown();
        setupButtons();

        return view;
    }

    private void setupDropdown() {
        // Selecting Mood Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.mood_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdownMood.setAdapter(adapter);
        // Social Situation Spinner setup
        ArrayAdapter<CharSequence> socialAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.social_situation_options,
                android.R.layout.simple_spinner_item
        );
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituation.setAdapter(socialAdapter);
    }

    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveMoodEvent());
        buttonCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void saveMoodEvent() {
        // Check if mood is at default position (0)
        if (dropdownMood.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Emotional state cannot be the default option", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if social situation is at default position (0)
        if (socialSituation.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Social situation cannot be the default option", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedEmotion = dropdownMood.getSelectedItem().toString();
        String socialSituation = editReason.getText().toString();
        String trigger = editTrigger.getText().toString();
        GeoPoint location = null; // needs to be implemented later
        String timeStamp = String.valueOf(new Date());
        boolean isLocationEnabled = switchLocation.isChecked();

        // Get the current user ID from Firebase Authentication
        //String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        String userId = username;
        if (userId == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new MoodEvent object
        MoodEvent moodEvent = new MoodEvent(selectedEmotion, trigger,socialSituation, timeStamp, location);

        // Build a map to save to Firestore
        Map<String, Object> moodEventData = new HashMap<>();
        moodEventData.put("mood", moodEvent.getEmotionalState());
        moodEventData.put("timestamp", moodEvent.getTimestamp());
        moodEventData.put("location", isLocationEnabled ? getUserLocation() : null);
        moodEventData.put("trigger", moodEvent.getTrigger());
        moodEventData.put("socialSituation", moodEvent.getSocialSituation());

        // Save the mood event under the current user's moodEvents subcollection
        db.collection("Users")
                .document(userId); // Get the current user's document


        db.collection("Mood Events")
                .add(moodEventData)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added the document to Firestore
                    Toast.makeText(getContext(), "Mood event saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failed to add the document
                    Toast.makeText(getContext(), "Error saving mood event", Toast.LENGTH_SHORT).show();
                });
    }

    private Map<String, Double> getUserLocation() {
        // Placeholder: Get the user's location if location tracking is enabled
        Map<String, Double> location = new HashMap<>();
        location.put("latitude", 53.5);  // Example latitude
        location.put("longitude", -113.5); // Example longitude
        return location;
    }
}
