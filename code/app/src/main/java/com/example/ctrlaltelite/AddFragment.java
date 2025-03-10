package com.example.ctrlaltelite;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

// Import the MoodEvent class


/**
 * Fragment for adding a mood event.
 * Users can select a mood, provide a reason and a trigger, upload an image, specify a social situation, and choose to attach their location.
 */
public class AddFragment extends Fragment {

    /** Spinner for selecting mood */
    private Spinner dropdownMood;
    /** Spinner for selecting social situation (optional) */
    private Spinner editSocialSituation;
    /** EditText for entering reason */
    protected EditText editReason;
    /** EditText for entering reason */
    private EditText editTrigger;
    /** Switch for enabling location tracking */
    private Switch switchLocation;
    /** Buttons for saving, canceling, and uploading an image */
    private Button buttonSave, buttonCancel, buttonUpload;
    /** Username of the logged-in user */
    private  String username;

    /** Firebase Firestore database instance */
    protected FirebaseFirestore db;
    /** Firebase Authentication instance */
    protected FirebaseAuth mAuth;

    /** Firebase Storage instance for image uploads */
    protected FirebaseStorage storage;
    private StorageReference storageRef;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    /** Reference to the uploaded image */
    protected Uri imageRef = null;

    private String imgPath = null; //default, no image
    private ImageView imagePreview;

    /** Max allowed image size in bytes */
    private int maxSize = 65536;



    /**
     * Initializes fragment and registers media pickers.
     * @param savedInstanceState The saved state of the fragment.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri == null) {
                        Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
                    } else {
                        //check file size and ensure less than 65536 bytes
                        Cursor returnCursor = getContext().getContentResolver().query(uri, null, null, null, null);
                        if (returnCursor != null) {
                            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                            returnCursor.moveToFirst();
                            int imgSize = returnCursor.getInt(sizeIndex);
                            returnCursor.close();
                            if (imgSize < maxSize) {
                                imagePreview.setImageURI(uri);
                                imagePreview.setVisibility(VISIBLE);
                                buttonUpload.setEnabled(false);
                                imageRef = uri; //for uploading purposes
                                Toast.makeText(getContext(), "Image selected", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "File exceeds max size", Toast.LENGTH_SHORT).show();
                            }
                        }}
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

    /**
     * Creates and returns the view for the fragment.
     * @param inflater Layout inflater
     * @param container View container
     * @param savedInstanceState The saved state of the fragment
     * @return The created View
     */

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

    /** Sets up the mood selection dropdown. */
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

    /** Sets up the social situation dropdown. */
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

    /** Sets up button click listeners. */
    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveMoodEvent(username));
        buttonCancel.setOnClickListener(v -> {
            // Navigate to the home screen fragment using the parent activity's method
            navigateToHome();
        });
        /**
         * Connects the Upload Media button to the functionality of selecting a photo
         */
        buttonUpload.setOnClickListener(v -> selectPhoto());
    }

    /**
     * This is a function that checks for gallery access permissions and starts a photo picker
     */
    private void selectPhoto() {
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

    /** Navigates back to the home fragment. */
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

    /**
     * Saves a mood event, ensuring a reason or image is provided.
     * @param uName The username of the logged-in user.
     */
    protected void saveMoodEvent(String uName) {
        if (dropdownMood.getSelectedItemPosition() == 0) {
            Toast.makeText(getContext(), "Emotional state cannot be the default option", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedEmotion = dropdownMood.getSelectedItem().toString();
        String socialSituation = editSocialSituation.getSelectedItemPosition() == 0 ? null : editSocialSituation.getSelectedItem().toString();
        String trigger = editTrigger.getText().toString();
        String timeStamp = String.valueOf(new Date());
        GeoPoint location = switchLocation.isChecked() ? getUserLocation() : null;


        // Obtaining the textual reason from the user
        String reason = editReason.getText().toString().trim();

        // Separator
        String separator = " ";

        // Separating the reason by spaces
        String[] separationArray = reason.split(separator);

        // Trying to implement setError instead of Toast Messages (will try to figure out later)
        /*
        if (reason.length() <= 20 || separationArray.length >= 4) {
            editReason.setError("Reason Length Cannot Have More than 20 Characters or Have More Than 3 Words");
            return;
        }
        */

        // Ensure either text reason or image is provided
        if (reason.isEmpty() && imageRef == null) {
            Toast.makeText(getContext(), "Either a reason or an image must be provided", Toast.LENGTH_SHORT).show();
            return;
        }

        // Adding the conditions for the textual reason
        if (reason.length() > 20) {
            Toast.makeText(getContext(), "Reason cannot have more than 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (separationArray.length >= 4) {
            Toast.makeText(getContext(), "Reason cannot be more than 4 words", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isLocationEnabled = switchLocation.isChecked();
        // Get the current user ID from Firebase Authentication
        //String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        String userId = username;
        if (userId == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        MoodEvent moodEvent = new MoodEvent(selectedEmotion, reason, trigger, socialSituation, timeStamp, location, null, username);
        if (imageRef != null) {
            String imgPath = "images/" + userId + timeStamp + ".png";
            StorageReference fileRef = storageRef.child(imgPath);
            fileRef.putFile(imageRef)
                    .addOnSuccessListener(taskSnapshot -> {
                        moodEvent.setImgPath(imgPath);
                        saveToFirestore(moodEvent);
                    })
                    .addOnFailureListener(error -> {
                        Toast.makeText(getContext(), "Error uploading image, saving without image", Toast.LENGTH_SHORT).show();
                        saveToFirestore(moodEvent);
                    });
        } else {
            saveToFirestore(moodEvent);
        }
    }

    // Add this new method to handle Firestore saving and set the docId
    /**
     * Saves the mood event to Firestore.
     * @param moodEvent The mood event to save.
     */
    private void saveToFirestore(MoodEvent moodEvent) {
        db.collection("Mood Events")
                .add(moodEvent)
                .addOnSuccessListener(documentReference -> {
                    moodEvent.setDocumentId(documentReference.getId());
                    Log.d("AddFragment", "Saved MoodEvent with docId: " + moodEvent.getDocumentId() + ", imgPath: " + moodEvent.getImgPath());
                    Toast.makeText(getContext(), "Mood event saved!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error saving mood event", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Gets the user's current location as a GeoPoint.
     * @return The user's location.
     */
    private GeoPoint getUserLocation() {
        // Example latitude and longitude values
        double latitude = 53.5;
        double longitude = -113.5;

        return new GeoPoint(latitude, longitude);
    }
}
