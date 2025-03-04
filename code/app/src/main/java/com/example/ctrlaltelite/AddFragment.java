package com.example.ctrlaltelite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;


import java.util.Date;
import java.util.Map;
import java.util.HashMap;

// Import the MoodEvent class


public class AddFragment extends Fragment {

    private Spinner dropdownMood;
    private Spinner editSocialSituation;
    private EditText editReason, editTrigger;
    private Switch switchLocation;
    private Button buttonSave, buttonCancel, buttonUpload;
    private  String username;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    //private StorageReference storageRef;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private Uri imageRef;
    private ImageView imagePreview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri == null) {
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    } else {
                        imageRef = uri; //for uploading purposes
                        imagePreview.setImageURI(uri);
                        imagePreview.setVisibility(VISIBLE);
                        buttonUpload.setEnabled(false);
                        Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
                    }
                });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        // Start the photo picker (only images).
                        pickMedia.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else {
                        Toast.makeText(getContext(), "No access to device images", Toast.LENGTH_SHORT).show();
                    }
                });
    }

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
        editSocialSituation = view.findViewById(R.id.social_situation_spinner);
        editReason = view.findViewById(R.id.edit_reason);
        editTrigger = view.findViewById(R.id.edit_trigger);
        switchLocation = view.findViewById(R.id.switch_location);
        buttonSave = view.findViewById(R.id.button_save);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonUpload = view.findViewById(R.id.button_upload);
        imagePreview = view.findViewById(R.id.uploaded_image);
        imagePreview.setVisibility(GONE);

        setupDropdown();
        setupButtons();
        setupDropdownSocialSituation();

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

    }

    private  void setupDropdownSocialSituation(){
        // Social Situation Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.social_situation_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editSocialSituation.setAdapter(adapter);
    }
    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveMoodEvent(username));
        buttonCancel.setOnClickListener(v -> {
            // Navigate to the home screen fragment using the parent activity's method
            navigateToHome();
        });
        buttonUpload.setOnClickListener(v -> uploadPhoto());
    }

    private void uploadPhoto() {
        //If app has permission
        if (ContextCompat.checkSelfPermission(
                getContext(), android.Manifest.permission.READ_MEDIA_IMAGES) ==
                PackageManager.PERMISSION_GRANTED) {
            // Start the photo picker (only images).
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            // Ask for the permission
            requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
        }
    }


    private void navigateToHome() {
        if (getActivity() instanceof MainActivity) {
            // First, change the fragment
            ((MainActivity) getActivity()).fragmentRepl(new HomeFragment());

            // Then, update the bottom navigation to select the home item
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.btnNav);
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.home);
            }
        }
    }

    private void saveMoodEvent(String uName) {
        // Check if mood is at default position (0)
        if (dropdownMood.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Emotional state cannot be the default option", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if social situation is at default position (0)
        if (editSocialSituation.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Social situation cannot be the default option", Toast.LENGTH_SHORT).show();
            return;
        }
        String selectedEmotion = dropdownMood.getSelectedItem().toString();
        String reason = editReason.getText().toString();
        String socialSituation = editSocialSituation.getSelectedItem().toString();
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

        //Upload to Firestore and save reference in db
        //StorageReference fileRef = storageRef.child(ID+".png");
        //fileRef.putFile(imageRef).addOnFailureListener(error ->
        // {
            //Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT),show();
        // }
        
        // Create a new MoodEvent object
        MoodEvent moodEvent = new MoodEvent(selectedEmotion, reason, trigger,socialSituation, timeStamp, location);

        // Build a map to save to Firestore
        Map<String, Object> moodEventData = new HashMap<>();
        moodEventData.put("mood", moodEvent.getEmotionalState());
        moodEventData.put("reason", moodEvent.getReason());
        moodEventData.put("timestamp", moodEvent.getTimestamp());
        moodEventData.put("location", isLocationEnabled ? getUserLocation() : null);
        moodEventData.put("trigger", moodEvent.getTrigger());
        moodEventData.put("socialSituation", moodEvent.getSocialSituation());
        moodEventData.put("Username", userId);
        //moodEventData.put("imageRef", moodEvent.getImage());


        db.collection("Mood Events")
                .add(moodEventData)
                .addOnSuccessListener(documentReference -> {
                    // Successfully added the document to Firestore
                    Toast.makeText(getContext(), "Mood event saved!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    // Failed to add the document
                    Toast.makeText(getContext(), "Error saving mood event", Toast.LENGTH_SHORT).show();
                });
    }

    private GeoPoint getUserLocation() {
        // Example latitude and longitude values
        double latitude = 53.5;
        double longitude = -113.5;

        return new GeoPoint(latitude, longitude);
    }
}
