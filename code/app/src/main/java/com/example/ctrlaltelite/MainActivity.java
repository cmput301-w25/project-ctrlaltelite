package com.example.ctrlaltelite;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * The {@code MainActivity} serves as the entry point of the application and manages
 * navigation between different fragments using a bottom navigation bar.
 */
public class MainActivity extends AppCompatActivity {
    /** Firebase Firestore instance for potential database interactions. */
    private FirebaseFirestore db;

    /** Stores the logged-in user's username. */
    private String username;

    private TextView displaynameTextView,usernameTextView, emailTextView, phoneTextView;

    BottomNavigationView bottomNavigationView;

    DrawerLayout drawerLayout;
    NavigationView navigationView;



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

        // This finds the DrawerLayout in activity_main.xml
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Get the inflated header layout
        View headerView = navigationView.getHeaderView(0);

        //finding text views in that header layout
        TextView displaynameTextView = headerView.findViewById(R.id.text_display_name);
        TextView usernameTextView = headerView.findViewById(R.id.text_username);
        TextView emailTextView = headerView.findViewById(R.id.text_email);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            username = bundle.getString("username");
        }






        if (username != null && !username.isEmpty()) {
            db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Get the first matching document
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            // Extract fields (make sure these fields exist in your Firestore 'users' collection)
                            String displayName = document.getString("displayName");
                            String email = document.getString("email");
                            String mobile = document.getString("mobile");


                            // Storing the display name in SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("display_name", displayName);  // Save the logged-in user's display name
                            editor.apply();


                            // Fill your TextViews
                            displaynameTextView.setText(displayName);
                            usernameTextView.setText(username);  // or from Firestore if you store it there
                            emailTextView.setText(email != null ? email : "N/A");
                            if (phoneTextView != null) {
                                phoneTextView.setText(mobile != null ? mobile : "N/A");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("MainActivity", "Firestore error fetching user data", e);
                    });
        } else {
            Toast.makeText(MainActivity.this, "Username not provided", Toast.LENGTH_SHORT).show();
        }




        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemid = item.getItemId();

                if (itemid == R.id.profile_picture) {
                    Toast.makeText(MainActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();

                    // This replaces the current fragment with a new instance of ProfileFragment
                    fragmentRepl(new ProfileFragment());
                }

                if (itemid == R.id.logout) {
                    Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                    // Redirect to Login Screen
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                    startActivity(intent);

                }

                //whenever a method is called from here the side drawer will close
                drawerLayout.close();
                return false;
            }
        });





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
                    fragmentRepl(new FollowingNavButton());
                }

                else if (itemId == R.id.search) {
                    fragmentRepl(new SearchFragment(username));
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

    public void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
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