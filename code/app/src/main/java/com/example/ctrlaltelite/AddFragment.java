package com.example.ctrlaltelite;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;



import java.util.Arrays;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.content.Context;
import android.Manifest;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.widget.Toast;





import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Import the MoodEvent class


/**
 * Fragment for adding a mood event.
 * Users can select a mood, provide a reason and a trigger, upload an image, specify a social situation, and choose to attach their location.
 */
public class AddFragment extends Fragment implements LocationListener {

    /** Spinner for selecting mood */
    protected Spinner dropdownMood;
    /** Spinner for selecting social situation (optional) */
    protected Spinner editSocialSituation;
    /** EditText for entering reason */
    protected EditText editReason;
    /** EditText for entering reason */
    protected EditText editTrigger;
    /** Switch for enabling location tracking */
    protected Switch switchLocation;
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
    protected ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    protected ActivityResultLauncher<String> requestPermissionLauncher;

    /** Reference to the uploaded image */
    protected Uri imageRef = null;

    private String imgPath = null; //default, no image
    private ImageView imagePreview;

    /** Max allowed image size in bytes */
    private int maxSize = 65536;

    private LocationManager locationManager;



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
                        if (returnCursor != null && returnCursor.moveToFirst()) {
                            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
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
        // Convert the string-array to List<String>
        List<String> moodOptions = Arrays.asList(getResources().getStringArray(R.array.mood_options));

        // Use the custom adapter
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(requireContext(), moodOptions);
        dropdownMood.setAdapter(adapter);

    }

    /** Sets up the social situation dropdown. */
    private  void setupDropdownSocialSituation(){
        // Convert the array to List<String>
        List<String> socialOptions = Arrays.asList(getResources().getStringArray(R.array.social_situation_options));

        // Use the custom adapter for colored items
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(requireContext(), socialOptions);
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
    protected void selectPhoto() {
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

    protected void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explain why permission is needed and request it again
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Location Permission Required")
                        .setMessage("This app requires location permission to track your mood location.")
                        .setPositiveButton("OK", (dialog, which) ->
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100))
                        .setNegativeButton("Cancel", (dialog, which) ->
                                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show())
                        .show();
            } else {
                // Directly request permission
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location permission granted. Try saving again.", Toast.LENGTH_SHORT).show();
            } else {
                boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
                if (!showRationale) {
                    // User selected "Don't ask again," guide them to enable it manually
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Permission Required")
                            .setMessage("Location permission is necessary to use this feature. Please enable it in settings.")
                            .setPositiveButton("Go to Settings", (dialog, which) -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            })
                            .setNegativeButton("Cancel", (dialog, which) ->
                                    Toast.makeText(getContext(), "Location permission denied permanently", Toast.LENGTH_SHORT).show())
                            .show();
                } else {
                    Toast.makeText(getContext(), "Location permission denied. Try saving again to request permission.", Toast.LENGTH_SHORT).show();
                }
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

        GeoPoint location = null;  // Default to null



        // If location tracking is enabled, check for permission before fetching location
        if (switchLocation.isChecked()) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();  // Ask for permission
                return;  // Exit, don't save yet
            }
            location = getUserLocation();  // Fetch location only if permission is granted
        }


        String selectedEmotion = dropdownMood.getSelectedItem().toString().trim();
        String socialSituation = editSocialSituation.getSelectedItemPosition() == 0 ? null : editSocialSituation.getSelectedItem().toString();
        String trigger = editTrigger.getText().toString();
        Timestamp timeStamp = Timestamp.now();
//        GeoPoint location = switchLocation.isChecked() ? getUserLocation() : null;


        // Obtaining the textual reason from the user
        String reason = editReason.getText().toString().trim();

        // Separator
        String separator = " ";

        // Separating the reason by spaces
        String[] separationArray = reason.split(separator);

        if (!(isTextualReasonValid(reason))) {

            // Ensure either text reason or image is provided
            if (reason.isEmpty() && imageRef == null) {
                editReason.setError("Either a reason or an image must be provided");
                Toast.makeText(getContext(), "Either a reason or an image must be provided", Toast.LENGTH_SHORT).show();
                return;
            }

            // Adding the conditions for the textual reason
            if (reason.length() > 20) {
                editReason.setError("Reason cannot have more than 20 characters");
                Toast.makeText(getContext(), "Reason cannot have more than 20 characters", Toast.LENGTH_SHORT).show();
                return;
            } else if (separationArray.length >= 4) {
                editReason.setError("Reason cannot be more than 3 words");
                Toast.makeText(getContext(), "Reason cannot be more than 3 words", Toast.LENGTH_SHORT).show();
                return;
            }
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
            String imgPath = "images/" + userId + "_" + timeStamp.toDate().getTime() + ".png";

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
    protected void saveToFirestore(MoodEvent moodEvent) {
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
    protected GeoPoint getUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
            return null;
        }

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        // Remove old location updates
        locationManager.removeUpdates(this);

        // Request fresh location updates
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("Location Debug", "Updated Latitude: " + latitude + ", Longitude: " + longitude);

                // Store the updated location
                GeoPoint geoPoint = new GeoPoint(latitude, longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(getContext(), "GPS is turned off!", Toast.LENGTH_SHORT).show();
            }
        }, null);


        // Get the last known location (may be outdated but prevents null return)
        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            Log.d("Location Debug", "Last Known Latitude: " + latitude + ", Longitude: " + longitude);
            return new GeoPoint(latitude, longitude);
        } else {
            Toast.makeText(getContext(), "Unable to get updated location", Toast.LENGTH_SHORT).show();
            return null;
        }
    }



    /**
     * Method to determine if textual reason is valid (mainly beneficial for unit testing)
     * @param textualReason the textual reason the user wants to type in
     * @return boolean value of whether or not the input textual reason is valid
     */
    public static boolean isTextualReasonValid(String textualReason) {
        // Separator
        String separator = " ";

        // Separating the reason by spaces
        String[] separationArray = textualReason.split(separator);

        return !(textualReason.isEmpty() || separationArray.length >= 4 || textualReason.length() >= 20);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Toast.makeText(getContext(), "New Location: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        Log.d("Location Debug", "Updated Latitude: " + latitude + ", Longitude: " + longitude);
    }

}
