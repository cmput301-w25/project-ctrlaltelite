package com.example.ctrlaltelite;

import static android.text.TextUtils.isDigitsOnly;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SignUp activity handles new user registration.
 *
 * Users can enter their details including username, email, mobile number, and password.
 * The activity checks for uniqueness of the username against Firestore.
 * If the username is unique, the user's data is saved and the user is navigated to the Login activity.
 *
 */
public class SignUp extends AppCompatActivity {
    LottieAnimationView lottielogo;

    private EditText SUsername, SEmail, SMobile, SPassword,SDisplayName;
    private Button btnCreateAccount;
    private TextView tvLoginPrompt;
    private FirebaseFirestore db;
    // Email regex pattern for validating email addresses
    private String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";
    private Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);
        lottielogo = findViewById(R.id.lottielogo);


        // Initialize views from the sign-up layout
        SUsername = findViewById(R.id.SUsername);
        SDisplayName = findViewById(R.id.SDisplayName);
        SEmail = findViewById(R.id.SEmail);
        SMobile = findViewById(R.id.SMobile);
        SPassword = findViewById(R.id.SPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLoginPrompt = findViewById(R.id.tvLoginPrompt);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the account creation button click event.
             * Validates input fields and checks for the uniqueness of the username in Firestore.
             * On successful registration, navigates the user to the Login activity.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String username = SUsername.getText().toString().trim();
                String displayName = SDisplayName.getText().toString().trim();
                String email = SEmail.getText().toString().trim();
                String mobile = SMobile.getText().toString().trim();
                String password = SPassword.getText().toString().trim();

                // Validation Check
                if (!isSignUpDataValid(username,displayName, email, mobile, password)) {
                    if (username.isEmpty()) {
                        SUsername.setError("Username cannot be empty!");
                        return;
                    }
                    if (displayName.isEmpty()) {
                        SDisplayName.setError("Display name cannot be empty!");
                        return;
                    }
                    if (email.isEmpty()) {
                        SEmail.setError("Email cannot be empty!");
                        return;
                    }
                    if (mobile.isEmpty()) {
                        SMobile.setError("Mobile number cannot be empty!");
                        return;
                    }
                    if (password.isEmpty()) {
                        SPassword.setError("Password cannot be empty!");
                        return;
                    }
                    // Validate that the mobile number is numeric
                    if (!isDigitsOnly(mobile)) {
                        Toast.makeText(SignUp.this, "Please enter a valid numeric phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // Validate the email address format using regex
                Matcher matcher = EMAIL_PATTERN.matcher(email);
                if (!matcher.matches()) {
                    SEmail.setError("Please enter a valid email address");
                    return;
                }
                if (mobile.length() != 10) {
                    SMobile.setError("Phone number must be 10 digits long");
                    return;
                }
                // Validate Password using regex
                Matcher matcherPass = PASSWORD_PATTERN.matcher(password);
                if (!matcherPass.matches()) {
                    SPassword.setError("Invalid Password, Must contain:\n" +
                            "- At least one letter\n" +
                            "- At least one digit\n" +
                            "- Minimum 8 characters");
                    return;
                }
                // Check if the username already exists in the "users" collection
                db.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Username already exists
                                SUsername.setError("Username already exists. Please Choose a different Username");
                            } else {
                                // Username is unique
                                Map<String, Object> user = new HashMap<>();
                                user.put("username", username);
                                user.put("displayName", displayName);
                                user.put("email", email);
                                user.put("mobile", mobile);
                                user.put("password", password);

                                // Save the user info to Firestore
                                db.collection("users")
                                        .add(user)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(SignUp.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                            // Navigate to Login page
                                            Intent intent = new Intent(SignUp.this, Login.class);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(SignUp.this, "Error creating account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(SignUp.this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Create a clickable "Login" text with purple color
        String promptText = "Already have an account? Login";
        SpannableString spannableString = new SpannableString(promptText);

        // Define clickable span for the "Login" word
        ClickableSpan clickableSpan = new ClickableSpan() {
            /**
             * Handles the click event for the Login prompt.
             * Navigates the user to the Login activity.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                // Navigate to the Login page when "Login" is clicked
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish(); // Optionally close the SignUp activity
            }
        };

        ForegroundColorSpan purpleSpan = new ForegroundColorSpan(0xFF800080); // Purple color (#800080)

        int startIndex = promptText.indexOf("Login");
        int endIndex = startIndex + "Login".length();

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(purpleSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginPrompt.setText(spannableString);
        tvLoginPrompt.setMovementMethod(LinkMovementMethod.getInstance());
    }
    public static boolean isSignUpDataValid(String username, String displayName, String email, String mobile, String password) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        if (displayName == null || displayName.trim().isEmpty()){
            return false;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            return false;
        }
        if (mobile == null || mobile.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}