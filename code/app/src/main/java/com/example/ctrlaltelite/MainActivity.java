package com.example.ctrlaltelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private String username;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.btnNav);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Get username and password from the Bundle
            username = bundle.getString("username");
        }

        fragmentRepl(new HomeFragment());




        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();
                Fragment selectedFragment = null;

                if (itemId == R.id.home) {
                    fragmentRepl(new HomeFragment());
                }


                else if (itemId == R.id.map) {
                    fragmentRepl(new MapFragment());
                }



                else if (itemId == R.id.add) {

                    AddFragment addFragment = new AddFragment();
                    Bundle fragmentBundle = new Bundle();
                    fragmentBundle.putString("username", username);  // Pass the username
                    addFragment.setArguments(fragmentBundle);
                    selectedFragment = addFragment;

                }


                else if (itemId == R.id.following) {
                    fragmentRepl(new FollowingFragment());
                }


                else if (itemId == R.id.profile) {
                    fragmentRepl(new ProfileFragment());
                }


                if (selectedFragment != null) {
                    fragmentRepl(selectedFragment);
                }



                return true;
            }
        });


    }

    private void fragmentRepl(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();


    }




}