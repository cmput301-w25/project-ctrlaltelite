package com.example.ctrlaltelite;

import android.content.Intent;
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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private EditText SUsername, SEmail, SMobile, SPassword;
    private Button btnCreateAccount;
    private TextView tvLoginPrompt;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        // Initialize views from the sign-up layout
        SUsername = findViewById(R.id.SUsername);
        SEmail = findViewById(R.id.SEmail);
        SMobile = findViewById(R.id.SMobile);
        SPassword = findViewById(R.id.SPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvLoginPrompt = findViewById(R.id.tvLoginPrompt);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = SUsername.getText().toString().trim();
                String email = SEmail.getText().toString().trim();
                String mobile = SMobile.getText().toString().trim();
                String password = SPassword.getText().toString().trim();

                // Validation Check
                if (username.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if the username already exists in the "users" collection
                    db.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    // Username already exists
                                    Toast.makeText(SignUp.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Username is unique
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", username);
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
            }
        });

        // Create a clickable "Login" text with purple color
        String promptText = "Already have an account? Login";
        SpannableString spannableString = new SpannableString(promptText);

        // Define clickable span for the "Login" word
        ClickableSpan clickableSpan = new ClickableSpan() {
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
}
