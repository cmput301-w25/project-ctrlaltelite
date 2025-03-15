package com.example.ctrlaltelite;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;

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
/**
 * The {@code MainActivity} serves as the entry point of the application and manages
 * navigation between different fragments using a bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {
    /** Firebase Firestore instance for potential database interactions. */
    private FirebaseFirestore db;

    /** Stores the logged-in user's username. */
    private String username;
    BottomNavigationView bottomNavigationView;

    /**
     * Called when the activity is first created. It initializes the UI, retrieves
     * user information, and sets up the bottom navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     *                           being shut down, this contains the saved state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        bottomNavigationView = findViewById(R.id.btnNav);


        View decoreView = getWindow().getDecorView();
        decoreView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsets onApplyWindowInsets(@NonNull View v, @NonNull WindowInsets
                    insets) {
                int left = insets.getSystemWindowInsetLeft();
                int top = insets.getSystemWindowInsetTop();
                int right = insets.getSystemWindowInsetRight();
                int bottom = insets.getSystemWindowInsetBottom();
                v.setPadding(left,top,right,bottom);
                return insets.consumeSystemWindowInsets();
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Get username and password from the Bundle
            username = bundle.getString("username");
        }

        fragmentRepl(new HomeFragment());

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            /**
             * Handles navigation item selection by replacing the current fragment with the selected one.
             *
             * @param item The selected menu item.
             * @return {@code true} if the navigation event is handled successfully.
             */
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

                else if (itemId == R.id.search) {
                    fragmentRepl(new SearchFragment());
                }


//                else if (itemId == R.id.profile) {
//                    ProfileFragment profileFragment = new ProfileFragment();
//                    Bundle args = new Bundle();
//                    args.putString("username", username); // Pass the username correctly
//                    profileFragment.setArguments(args);
//                    fragmentRepl(profileFragment);
//                }
                if (selectedFragment != null) {
                    fragmentRepl(selectedFragment);
                }
                return true;
            }
        });
    }

    /**
     * Replaces the current fragment with a new one and passes the username to the fragment.
     *
     * @param fragment The fragment to be displayed.
     */

    protected void fragmentRepl(Fragment fragment){
        Bundle bundle = fragment.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString("username", username);
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }
}