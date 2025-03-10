package com.example.ctrlaltelite;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * The Login activity handles user authentication.
 *
 * This activity allows users to input their username and password,
 * verifies their credentials against Firestore, and navigates to the
 * MainActivity upon successful login. If the user does not have an account,
 * they can click the sign-up prompt to navigate to the SignUp activity.
 *
 */
public class Login extends AppCompatActivity {

    private EditText Username;
    private EditText Password;
    private Button btnLogin;
    private TextView tvSignUpPrompt;
    private FirebaseFirestore db;

    /**
     * Called when the activity starts. Most initializations go here.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *        then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.button_login);
        tvSignUpPrompt = findViewById(R.id.tvSignUpPrompt);

        db = FirebaseFirestore.getInstance();
        btnLogin.setOnClickListener(new View.OnClickListener() {
            /**
             * Handles the login button click event.
             * Validates user input, verifies credentials against Firestore, and navigates to MainActivity upon success.
             *
             * @param view The view that was clicked.
             */
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString().trim();
                String password = Password.getText().toString().trim();

                if (!isInputValid(username, password)) {
                    if (username.isEmpty()) {
                        Username.setError("Username cannot be empty!");
                    }
                    if (password.isEmpty()) {
                        Password.setError("Password cannot be empty!");
                    }
                    return;
                }
                // Verify Credentials
                db.collection("users")
                        .whereEqualTo("username", username)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                Username.setError("User not found");
                            } else {
                                boolean valid = false;
                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                String storedPassword = document.getString("password");
                                if (storedPassword != null && storedPassword.equals(password)) {
                                    valid = true;
                                }
                                if (valid) {
                                    Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Bundle loginDetails = new Bundle();
                                    loginDetails.putString("username", username);
                                    // Navigate to Homepage
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    intent.putExtras(loginDetails);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
        // Create a clickable "SignUp" text
        String promptText = "Don't have an account? SignUp";
        SpannableString spannableString = new SpannableString(promptText);

        // Define clickable span
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                /**
                 * Handles the click event for the SignUp prompt.
                 *
                 * @param view The view that was clicked.
                 */
                // Go to the Login page when "Login" is clicked
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                finish();
            }
        };

        ForegroundColorSpan purpleSpan = new ForegroundColorSpan(Color.parseColor("#800080"));

        int startIndex = promptText.indexOf("SignUp");
        int endIndex = startIndex + "SignUp".length();

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(purpleSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSignUpPrompt.setText(spannableString);
        tvSignUpPrompt.setMovementMethod(LinkMovementMethod.getInstance());
    }
    public static boolean isInputValid(String username, String password) {
        return username != null && !username.trim().isEmpty() &&
                password != null && !password.trim().isEmpty();
    }
}