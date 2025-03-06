package com.example.ctrlaltelite;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private ListView listView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> moodEvents;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Register image picker in onCreate (only once)
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
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
        Spinner moodFilter = view.findViewById(R.id.mood_filter);
        ArrayAdapter<CharSequence> moodAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.mood_options,
                android.R.layout.simple_spinner_item
        );
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        moodFilter.setAdapter(moodAdapter);

        // Initialize ListView
        listView = view.findViewById(R.id.mood_list);
        if (listView == null) {
            Log.e("HomeFragment", "ListView is null, check fragment_home.xml");
            return view;
        }
        moodEvents = new ArrayList<>();
        adapter = new MoodEventAdapter(requireContext(), moodEvents);
        listView.setAdapter(adapter);

        // Set short click listener for editing
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            MoodEvent moodEvent = moodEvents.get(position);
            showDeleteEditMoodDialog(moodEvent, position);
        });
        // Fetch mood events
        fetchMoodEvents();

        return view;
    }

    private void fetchMoodEvents() {
        db.collection("Mood Events")
                .whereEqualTo("username", Username) // Matches Firestore field name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodEvents.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEvent moodEvent = document.toObject(MoodEvent.class);
                            String docId = document.getId();
                            moodEvent.setDocumentId(docId);
                            Log.d("HomeFragment", "Fetched MoodEvent with ID: " + docId + " - " + moodEvent.toString());
                            moodEvents.add(moodEvent);
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("HomeFragment", "Mood events fetched: " + moodEvents.size());
                    } else {
                        Log.w("HomeFragment", "Error fetching mood events", task.getException());
                        Toast.makeText(getContext(), "Error loading mood events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
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

        // Make upload button and image preview visible
        buttonUpload.setVisibility(View.VISIBLE);
        imagePreview.setVisibility(View.GONE);
        // Reset newImageRef for this dialog instance
        newImageRef = null;

        // Load existing image if available using Glide for better reliability
        if (moodEvent.getImgPath() != null) {
            StorageReference imageRef = storageRef.child(moodEvent.getImgPath());
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                Glide.with(requireContext())
                        .load(uri)
                        .into(imagePreview);
                imagePreview.setVisibility(View.VISIBLE);
            }).addOnFailureListener(e -> {
                Log.e("HomeFragment", "Failed to load image: " + e.getMessage());
                imagePreview.setVisibility(View.GONE);
            });
        } else {
            imagePreview.setVisibility(View.GONE);
            Log.d("NoImg", "Fetched MoodEvent: " + moodEvent.toString());
        }

        // Populate Mood Spinner
        ArrayAdapter<String> moodAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.mood_options));
        moodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
            String updatedMood = moodSpinner.getSelectedItem().toString();
            String updatedReason = reasonEditText.getText().toString().trim();
            String updatedTrigger = triggerEditText.getText().toString().trim();
            String updatedSocialSituation = socialSituationSpinner.getSelectedItem().toString();
            if (!updatedMood.isEmpty()) {
                moodEvent.setEmotionalState(updatedMood);
                moodEvent.setReason(updatedReason);
                moodEvent.setTrigger(updatedTrigger);
                moodEvent.setSocialSituation(updatedSocialSituation);
                // Set the current timestamp when saving
                java.text.DateFormat dateFormat = java.text.DateFormat.getDateTimeInstance(
                        java.text.DateFormat.MEDIUM,
                        java.text.DateFormat.MEDIUM,
                        java.util.Locale.getDefault() // Use local formatting
                );
                String currentTimestamp = dateFormat.format(new java.util.Date());
                moodEvent.setTimestamp(currentTimestamp);
                Log.d("HomeFragment", "Saving MoodEvent with docId: " + moodEvent.getDocumentId());
                if (newImageRef != null) {
                    String newImgPath = "images/" + Username + currentTimestamp + ".png";
                    StorageReference fileRef = storageRef.child(newImgPath);
                    fileRef.putFile(newImageRef)
                            .addOnSuccessListener(taskSnapshot -> {
                                if (moodEvent.getImgPath() != null && !moodEvent.getImgPath().isEmpty()) {
                                    storageRef.child(moodEvent.getImgPath()).delete();
                                }
                                moodEvent.setImgPath(newImgPath);
                                updateMoodEventInFirestore(moodEvent, position);
                            })
                            .addOnFailureListener(e -> {
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

    private void DeleteMoodEventAndUpdateDatabaseUponDeletion(MoodEvent moodEvent) {

        CollectionReference moodEventRef = db.collection("Mood Events");

        fetchMoodEvents();

        moodEventRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                moodEvents.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String emotionalState = snapshot.getString("emotionalState");
                    String reason = snapshot.getString("reason");
                    String socialSituation = snapshot.getString("socialSituation");
                    String timeStamp = snapshot.getString("timestamp");
                    String trigger = snapshot.getString("trigger");
                    String username = snapshot.getString("username");
                    String imgPath = snapshot.getString("imgPath");
                    String id = snapshot.getId();
                    GeoPoint location = (GeoPoint) snapshot.get("location");
                    MoodEvent updatedMoodEvent = new MoodEvent(emotionalState, reason, trigger, socialSituation, timeStamp, location, imgPath, username);
                    moodEvents.add(updatedMoodEvent);
                    updatedMoodEvent.setDocumentId(id);
                }
                adapter.notifyDataSetChanged();
            }
        });
        if (moodEvents.isEmpty()) {
            Toast.makeText(getContext(), "Zero length", Toast.LENGTH_SHORT).show();
        }
        else {
            DocumentReference docRef = moodEventRef.document(moodEvent.getDocumentId());
            docRef.delete();
        }
    }

    // Update the mood event in Firebase
    private void updateMoodEventInFirestore(MoodEvent moodEvent, int position) {
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
                    Log.d("HomeFragment", "Successfully updated MoodEvent with ID: " + docId + " in Firestore");
                    moodEvents.set(position, moodEvent);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Mood updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Failed to update MoodEvent with ID: " + docId + " - " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to update mood in Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}