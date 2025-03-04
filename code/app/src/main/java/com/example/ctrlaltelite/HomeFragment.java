package com.example.ctrlaltelite;
import android.os.Bundle;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListView listView;
    private MoodEventAdapter adapter;
    private List<MoodEvent> moodEvents;
    private FirebaseFirestore db;
    private String Username;

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
            showEditMoodDialog(moodEvent, position);
        });

        // Fetch mood events
        fetchMoodEvents();

        return view;
    }

    private void fetchMoodEvents() {
        db.collection("Mood Events")
                .whereEqualTo("Username", Username) // Matches Firestore field name
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
    public void showEditMoodDialog(MoodEvent moodEvent, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_delete_mood_event, null);
        builder.setView(dialogView);

        // Bind views
        TextView closeButton = dialogView.findViewById(R.id.close_button);
        // Button buttonUpload = dialogView.findViewById(R.id.edit_upload_media_button);
        // ImageView image = dialogView.findViewById(R.id.edit_uploaded_image);
        Spinner moodSpinner = dialogView.findViewById(R.id.edit_mood_spinner);
        EditText reasonEditText = dialogView.findViewById(R.id.edit_reason_edittext);
        EditText triggerEditText = dialogView.findViewById(R.id.edit_trigger);
        Spinner socialSituationSpinner = dialogView.findViewById(R.id.edit_social_situation_spinner);
        Button saveButton = dialogView.findViewById(R.id.save_button);

        // buttonUpload.setVisibility(View.VISIBLE);
        // image.setVisibility(View.VISIBLE);

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

        AlertDialog dialog = builder.create();

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
                moodEvent.setTimestamp(currentTimestamp);
                updateMoodEventInFirestore(moodEvent, position);
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Mood is required", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
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