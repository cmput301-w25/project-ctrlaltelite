package com.example.ctrlaltelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private EditText Username;
    private EditText Password;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);
        btnLogin = findViewById(R.id.button_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = Username.getText().toString().trim();
                String password = Password.getText().toString().trim();

                // Dummy check for example purposes
                if (username.equals("admin") && password.equals("password")) {
                    Toast.makeText(com.example.ctrlaltelite.Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                    // Navigate to MainActivity (or your main app screen)
                    Intent intent = new Intent(com.example.ctrlaltelite.Login.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(com.example.ctrlaltelite.Login.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
