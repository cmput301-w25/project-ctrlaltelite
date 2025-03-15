package com.example.ctrlaltelite;

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
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Calendar;

/**
 * Home Fragment displays and manages a user's mood history - personal mood events, allowing viewing, editing and delete.
 * Integrates with Firebase Firestore for data storage and Firebase Storage for image handling.
 */
public class HomeFragment extends AddFragment {
    private GeoPoint updatedLocation;
    private boolean isSorted = false; // Tracks sorting state

    private ListView listView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> moodEvents = new ArrayList<>();
    private FirebaseFirestore db;
    private String Username;

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
     * This is called to do initial creation of the fragment. It initializes Firebase Storage and registers
     * the image picker and permission request handlers.
     *
     * @param savedInstanceState If non-null, this fragment is re-constructed from a previous
     *                           saved state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();





        // Register image picker in onCreate (only once)
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            /**
             * Handles the result of the image picker for editing mood events.
             *
             * @param uri The URI of the selected image, or null if no image was selected.
             */
            if (uri == null) {
                Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            } else {
                Cursor returnCursor = requireContext().getContentResolver().query(uri, null, null, null, null);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                int imgSize = returnCursor.getInt(sizeIndex);
                returnCursor.close();

                if (imgSize < MAX_SIZE) {
                    if (imagePreview != null) { // Ensure imagePreview is set
                        // Use Glide to load the selected local image into imagePreview immediately
                        Glide.with(requireContext())
                                .load(uri)
                                .into(imagePreview);
                        imagePreview.setVisibility(View.VISIBLE);
                    }
                    newImageRef = uri; // Store the new image URI for use in save
                    Toast.makeText(getContext(), "New image selected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "File exceeds max size (64KB)", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Register permission request
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            /**
             * Handles the result of the permission request for accessing media images.
             *
             * @param isGranted True if permission was granted, false otherwise.
             */
            if (isGranted) {
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();

        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
            Log.d("MoodHistoryFragment", "Fetching mood events for Username: " + Username);
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
        if (listView == null) {
            Log.e("HomeFragment", "ListView is null, check fragment_home.xml");
            return view;
        }
        //moodEvents = new ArrayList<>();
        adapter = new MoodEventAdapter(requireContext(), moodEvents);
        listView.setAdapter(adapter);
        fetchMoodEvents(); // Start fetching data first to ensure data is available before UI interactions
        // Moved Spinner listener after fetchMoodEvents() to avoid applyFilters() running before data is fetched
        moodFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                moodFilter = moodFilterOptions.get(position);
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Week filter
        weekFilterCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            weekFilter = isChecked;
            applyFilters();
        });

        // Reason filter
        reasonFilterEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                reasonFilter = s.toString().trim().toLowerCase();
                applyFilters(); // Apply filters immediately on text change
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set short click listener for editing
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            MoodEvent moodEvent = moodEvents.get(position);
            showDeleteEditMoodDialog(moodEvent, position);
        });

        return view;
    }

    private void applyFilters() {
        if (allMoodEvents == null || allMoodEvents.isEmpty()) {
            moodEvents.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        for (MoodEvent event : allMoodEvents) {
            Log.d("HomeFragment", "Pre-filter event: emotionalState='" + event.getEmotionalState() + "', reason='" + event.getReason() + "'");
        }

        // Modify the existing moodEvents list in place instead of reassigning
        moodEvents.clear();
        moodEvents.addAll(allMoodEvents);

        if (!moodFilter.equals("Mood")) {
            Log.d("HomeFragment", "Filtering with moodFilter='" + moodFilter + "'");
            List<MoodEvent> filteredList = moodEvents.stream()
                    .filter(event -> {
                        String emotionalState = event.getEmotionalState();
                        boolean matches = emotionalState.trim().equals(moodFilter.trim());
                        Log.d("HomeFragment", "Checking mood: '" + emotionalState + "' vs '" + moodFilter + "' -> " + matches);
                        return matches;
                    })
                    .collect(Collectors.toList());
            moodEvents.clear();
            moodEvents.addAll(filteredList);
            Log.d("HomeFragment", "After mood filter: " + moodEvents.size() + " events");
        }

        if (weekFilter) {
            Calendar oneWeekAgo = Calendar.getInstance();
            oneWeekAgo.add(Calendar.DAY_OF_YEAR, -7);
            long oneWeekAgoMillis = oneWeekAgo.getTimeInMillis();
            List<MoodEvent> filteredList = moodEvents.stream()
                    .filter(event -> event.getTimestamp().toDate().getTime() >= oneWeekAgoMillis)
                    .collect(Collectors.toList());
            moodEvents.clear();
            moodEvents.addAll(filteredList);
        }

        if (!reasonFilter.isEmpty()) {
            List<MoodEvent> filteredList = moodEvents.stream()
                    .filter(event -> Arrays.asList(event.getReason().toLowerCase().split("\\s+"))
                    .contains(reasonFilter))
                    .collect(Collectors.toList());
            moodEvents.clear();
            moodEvents.addAll(filteredList);
            Log.d("HomeFragment", "After reason filter: " + moodEvents.size() + " events");
        }

        moodEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
        adapter.notifyDataSetChanged(); // Notify adapter of changes to the existing list
    }

    public void toggleSort() {
        if (moodEvents == null || moodEvents.isEmpty()) return;
        moodEvents.sort((a, b) -> Long.compare(b.getTimestamp().toDate().getTime(), a.getTimestamp().toDate().getTime()));
        adapter.notifyDataSetChanged();
    }



    /**
     * Listens for real-time updates to the mood events associated with the current user in Firestore.
     * Automatically updates the local list and UI adapter whenever changes occur in the database.
     */

    public void fetchMoodEvents() {
        db.collection("Mood Events")
                .whereEqualTo("username", Username)
                .get() // Fetch data initially
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allMoodEvents.clear(); // Reset full list
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            moodEvent.setDocumentId(document.getId()); // Ensure docId is set
                            allMoodEvents.add(moodEvent);
                        }

                        // Populate moodEvents initially (before filtering)
                        moodEvents.clear();
                        moodEvents.addAll(allMoodEvents);

                        // Sort and Refresh UI
                        toggleSort();
                        adapter.notifyDataSetChanged();

                        Log.d("HomeFragment", "Initial Mood events fetched: " + allMoodEvents.size());
                    } else {
                        Log.w("HomeFragment", "Error fetching mood events", task.getException());
                    }
                });

        // 🔄 Listen for real-time changes in Firestore
        db.collection("Mood Events")
                .whereEqualTo("username", Username)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("HomeFragment", "Firestore Listen Failed: " + error.toString());
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
                        moodEvents.clear();
                        moodEvents.addAll(allMoodEvents);

                        // Sort and Refresh UI After Updating the List
                        toggleSort();
                        adapter.notifyDataSetChanged();

                        Log.d("HomeFragment", "Mood events updated in real-time: " + allMoodEvents.size());
                    }
                });
    }


    /**
     * Validates if the mood input is non-empty.
     *
     * @param mood The mood string to validate.
     * @return True if the mood is non-empty, false otherwise.
     */
    public boolean isMoodValid(String mood) {
        return mood != null && !mood.trim().isEmpty() &&
                !mood.equals("😐 Select Emotional State");
    }





    /**
     * Displays a dialog allowing the user to edit or delete a specific mood event.
     * Pre-populates the dialog with the mood event's current data and handles image uploads.
     *
     * @param moodEvent The MoodEvent object to be edited or deleted.
     * @param position  The position of the mood event in the list, used for updating the adapter.
     */
    public void showDeleteEditMoodDialog(MoodEvent moodEvent, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_delete_mood_event, null);
        builder.setView(dialogView);

        // Bind views
        TextView closeButton = dialogView.findViewById(R.id.close_button);
        Button buttonUpload = dialogView.findViewById(R.id.edit_upload_media_button);
        ImageView imagePreview = dialogView.findViewById(R.id.edit_uploaded_image);
        Spinner moodSpinner = dialogView.findViewById(R.id.edit_mood_spinner);
        EditText reasonEditText = dialogView.findViewById(R.id.edit_reason_edittext);
        EditText triggerEditText = dialogView.findViewById(R.id.edit_trigger);
        Spinner socialSituationSpinner = dialogView.findViewById(R.id.edit_social_situation_spinner);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button deleteButton = dialogView.findViewById(R.id.delete_button);
        Switch switchLocation = dialogView.findViewById(R.id.edit_location_switch);

        // Enable toggle by default if location exists
        if (moodEvent.getLocation() != null) {
            switchLocation.setChecked(true);
        } else {
            switchLocation.setChecked(false);
        }





        // Make upload button and image preview visible
        buttonUpload.setVisibility(View.VISIBLE);
        Glide.with(requireContext()).clear(imagePreview);
        imagePreview.setVisibility(View.GONE);
        // Reset newImageRef for this dialog instance
        newImageRef = null;

        // Load existing image if available using Glide for better reliability
        if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
            StorageReference imageRef = storageRef.child(moodEvent.getImgPath());
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                /**
                 * Loads the existing image into the preview when the download URL is successfully retrieved.
                 *
                 * @param uri The URI of the image downloaded from Firebase Storage.
                 */
                Glide.with(requireContext())
                        .load(uri)
                        .into(imagePreview);
                imagePreview.setVisibility(View.VISIBLE);
            }).addOnFailureListener(e -> {
                /**
                 * Handles failure to retrieve the image download URL from Firebase Storage.
                 *
                 * @param e The exception indicating the reason for failure.
                 */
                Log.e("HomeFragment", "Failed to load image: " + e.getMessage());
                Glide.with(requireContext()).clear(imagePreview);
                imagePreview.setVisibility(View.GONE);
            });
        } else {
            Glide.with(requireContext()).clear(imagePreview);
            imagePreview.setVisibility(View.GONE);
            Log.d("NoImg", "Fetched MoodEvent: " + moodEvent.toString());
        }

        List<String> moodOptionsList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.mood_options)));
        CustomSpinnerAdapter moodAdapter = new CustomSpinnerAdapter(requireContext(), moodOptionsList);
        moodSpinner.setAdapter(moodAdapter);
        int moodPosition = moodAdapter.getPosition(moodEvent.getEmotionalState());
        if (moodPosition >= 0) {
            moodSpinner.setSelection(moodPosition);
        }

        // Populate Social Situation Spinner from arrays.xml
        ArrayAdapter<String> socialAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.social_situation_options));
        socialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        socialSituationSpinner.setAdapter(socialAdapter);
        int socialPosition = socialAdapter.getPosition(moodEvent.getSocialSituation());
        if (socialPosition >= 0) {
            socialSituationSpinner.setSelection(socialPosition);
        }

        // Pre-fill EditText fields
        reasonEditText.setText(moodEvent.getReason());
        triggerEditText.setText(moodEvent.getTrigger());

        // Upload button listener (uses existing pickMedia)
        buttonUpload.setOnClickListener(v -> {
            /**
             * Initiates the image upload process when the upload button is clicked.
             *
             * @param v The view that was clicked (the upload button).
             */
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES) ==
                    PackageManager.PERMISSION_GRANTED) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
            }
        });

        AlertDialog dialog = builder.create();


        // Delete button
        deleteButton.setOnClickListener(v -> {
            DeleteMoodEventAndUpdateDatabaseUponDeletion(moodEvent);
            dialog.dismiss();
        });

        // Close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Save button
        saveButton.setOnClickListener(v -> {
            /**
             * Saves the edited mood event data when the save button is clicked.
             * Updates the mood event in Firestore and handles image upload if a new image is selected.
             *
             * @param v The view that was clicked (the save button).
             */

            String updatedMood = moodSpinner.getSelectedItem().toString();
            // Validate mood selection
            if (!isMoodValid(updatedMood)) {
                Toast.makeText(getContext(), "Emotional state cannot be the default option", Toast.LENGTH_SHORT).show();
                return;
            }

            String updatedReason = reasonEditText.getText().toString().trim();

            // Separator
            String separator = " ";

            // Separating the reason by spaces
            String[] separationArray = updatedReason.split(separator);

            // Ensure either text reason or image is provided
            if (updatedReason.isEmpty()) {
                reasonEditText.setError("Reason must be provided");
                //Toast.makeText(getContext(), "Reason must be provided", Toast.LENGTH_SHORT).show();
                return;
            }

            // Adding the conditions for the textual reason
            if (updatedReason.length() > 200) {
                reasonEditText.setError("Reason cannot have more than 200 characters");
                //Toast.makeText(getContext(), "Reason cannot have more than 20 characters", Toast.LENGTH_SHORT).show();
                return;
            } //else if (separationArray.length >= 4) {
                //reasonEditText.setError("Reason cannot be more than 3 words");
                //Toast.makeText(getContext(), "Reason cannot be more than 4 words", Toast.LENGTH_SHORT).show();
                //return;
            //}

            String updatedTrigger = triggerEditText.getText().toString().trim();
            String updatedSocialSituation = socialSituationSpinner.getSelectedItemPosition() == 0 ? null : socialSituationSpinner.getSelectedItem().toString();

            GeoPoint updatedLocation = moodEvent.getLocation();

            if (switchLocation.isChecked()) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission();
                    return; // 🚨 Exit early, wait for permission result
                } else {
                    updatedLocation = getUserLocation(); // Fetch new location
                }
            } else {
                updatedLocation = null; // If switch is off, remove location
            }


            if (isMoodValid(updatedMood)) {
                moodEvent.setEmotionalState(updatedMood);
                moodEvent.setReason(updatedReason);
                moodEvent.setTrigger(updatedTrigger);
                moodEvent.setSocialSituation(updatedSocialSituation);
                moodEvent.setLocation(updatedLocation);



                // Set the current timestamp when saving
                java.text.DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance(
                        java.text.DateFormat.MEDIUM,
                        java.text.DateFormat.MEDIUM,
                        java.util.Locale.getDefault() // Use local formatting
                );

                Timestamp currentTimestamp = Timestamp.now();
                moodEvent.setTimestamp(currentTimestamp);

                Log.d("HomeFragment", "Saving MoodEvent with docId: " + moodEvent.getDocumentId());
                if (newImageRef != null) {
                    String newImgPath = "images/" + Username + "_" + currentTimestamp.toDate().getTime() + ".png";
                    StorageReference fileRef = storageRef.child(newImgPath);
                    fileRef.putFile(newImageRef)
                            .addOnSuccessListener(taskSnapshot -> {
                                /**
                                 * Handles successful image upload to Firebase Storage and updates the mood event.
                                 *
                                 * @param taskSnapshot The snapshot of the upload task, containing metadata.
                                 */
                                if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
                                    storageRef.child(moodEvent.getImgPath()).delete();
                                } else {
                                    if (Objects.equals(moodEvent.getImgPath(), "null")) {
                                        imagePreview.setVisibility(View.GONE);
                                    }
                                    imagePreview.setVisibility(View.GONE);
                                }
                                moodEvent.setImgPath(newImgPath);
                                updateMoodEventInFirestore(moodEvent, position);
                            })
                            .addOnFailureListener(e -> {
                                /**
                                 * Handles failure to upload the image to Firebase Storage and proceeds without it.
                                 *
                                 * @param e The exception indicating the reason for failure.
                                 */
                                Toast.makeText(getContext(), "Image upload failed, saving without image", Toast.LENGTH_SHORT).show();
                                updateMoodEventInFirestore(moodEvent, position);
                            });
                } else {
                    updateMoodEventInFirestore(moodEvent, position);
                }
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Mood is required", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }


    /**
     * Function to delete mood events and update firebase and our mood events list accordingly
     * @param moodEvent - The mood event we want to delete
     */
    public void DeleteMoodEventAndUpdateDatabaseUponDeletion(MoodEvent moodEvent) {
        // Getting the mood events collection in Firestore
        CollectionReference moodEventRef = db.collection("Mood Events");

        // Deleting the mood event reference in Firestore
        DocumentReference docRef = moodEventRef.document(moodEvent.getDocumentId());
        docRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("HomeFragment", "MoodEvent deleted: " + moodEvent.getDocumentId());

            Toast.makeText(getContext(), "Mood event deleted!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.w("HomeFragment", "Error deleting mood event", e);
            Toast.makeText(getContext(), "Error deleting mood event", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates a mood event in Firestore with the provided MoodEvent data and refreshes the UI.
     * Logs success or failure and notifies the adapter of changes.
     *
     * @param moodEvent The MoodEvent object containing updated data to be saved in Firestore.
     * @param position  The position of the mood event in the list, used to update the adapter.
     */
    public void updateMoodEventInFirestore(MoodEvent moodEvent, int position) {
        String docId = moodEvent.getDocumentId();
        if (docId == null) {
            Log.e("HomeFragment", "Document ID is null for MoodEvent: " + moodEvent.toString());
            Toast.makeText(getContext(), "Cannot update: Invalid document ID", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d("HomeFragment", "Attempting to update MoodEvent with ID: " + docId);
        Log.d("HomeFragment", "MoodEvent data: " + moodEvent.toString());

        db.collection("Mood Events")
                .document(docId)
                .set(moodEvent)
                .addOnSuccessListener(aVoid -> {
                    /**
                     * Handles successful update of the mood event in Firestore and refreshes the UI.
                     *
                     * @param aVoid Void parameter (unused).
                     */
                    Log.d("HomeFragment", "Successfully updated MoodEvent with ID: " + docId + " in Firestore");
                    moodEvents.set(position, moodEvent);
                    toggleSort();
                    applyFilters();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Mood updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    /**
                     * Handles failure to update the mood event in Firestore and notifies the user.
                     *
                     * @param e The exception indicating the reason for failure.
                     */
                    Log.e("HomeFragment", "Failed to update MoodEvent with ID: " + docId + " - " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to update mood in Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
