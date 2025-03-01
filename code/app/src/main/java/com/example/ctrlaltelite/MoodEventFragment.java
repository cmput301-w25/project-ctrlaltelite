/*
package com.example.ctrlaltelite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Arrays;
import java.util.List;

public class MoodEventFragment extends Fragment {

    private AutoCompleteTextView dropdownMood;
    private EditText editReason, editTrigger;
    private Switch switchLocation;
    private Button buttonSave, buttonCancel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        // Initialize UI elements
        dropdownMood = view.findViewById(R.id.dropdown_mood);
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
        // Define Mood Options
        List<String> emotionalStates = Arrays.asList("Happy", "Sad", "Angry", "Excited", "Anxious");

        // Set up Adapter for Drop-down Menu
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, emotionalStates);
        dropdownMood.setAdapter(adapter);
    }

    private void setupButtons() {
        buttonSave.setOnClickListener(v -> saveMoodEvent());
        buttonCancel.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void saveMoodEvent() {
        String selectedEmotion = dropdownMood.getText().toString();
        String reason = editReason.getText().toString();
        String trigger = editTrigger.getText().toString();
        boolean isLocationEnabled = switchLocation.isChecked();

        // Display a confirmation message
        Toast.makeText(getContext(), "Mood event saved!", Toast.LENGTH_SHORT).show();
    }
}
*/