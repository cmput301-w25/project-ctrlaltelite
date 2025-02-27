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

public class SignUp extends AppCompatActivity {

    private EditText SUsername, SEmail, SMobile, SPassword;
    private Button btnCreateAccount;
    private TextView tvLoginPrompt;

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

        // Handle the Create Account button click
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = SUsername.getText().toString().trim();
                String email = SEmail.getText().toString().trim();
                String mobile = SMobile.getText().toString().trim();
                String password = SPassword.getText().toString().trim();

                // Simple validation check; replace with your actual account creation logic
                if (username.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUp.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUp.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to Homepage
                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Create a clickable "Login" text
        String promptText = "Already have an account? Login";
        SpannableString spannableString = new SpannableString(promptText);

        // Define clickable span
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Navigate to the Login page when "Login" is clicked
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        };

        ForegroundColorSpan purpleSpan = new ForegroundColorSpan(Color.parseColor("#800080"));

        int startIndex = promptText.indexOf("Login");
        int endIndex = startIndex + "Login".length();

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(purpleSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLoginPrompt.setText(spannableString);
        tvLoginPrompt.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
