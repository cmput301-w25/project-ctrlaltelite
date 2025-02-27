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

public class Login extends AppCompatActivity {

    private EditText Username;
    private EditText Password;
    private Button btnLogin;
    private TextView tvSignUpPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.button_login);
        tvSignUpPrompt = findViewById(R.id.tvSignUpPrompt);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString().trim();
                String password = Password.getText().toString().trim();

                // Dummy check
                if (username.equals("admin") && password.equals("password")) {
                    Toast.makeText(com.example.ctrlaltelite.Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                    // Go to Homepage
                    Intent intent = new Intent(com.example.ctrlaltelite.Login.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(com.example.ctrlaltelite.Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Create a clickable "SignUp" text
        String promptText = "Don't have an account? SignUp";
        SpannableString spannableString = new SpannableString(promptText);

        // Define clickable span
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
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
}
