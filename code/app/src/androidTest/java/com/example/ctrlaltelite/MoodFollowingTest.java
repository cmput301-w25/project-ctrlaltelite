package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * UI tests for mood history filtering functionalities (US 04.02.01, 04.03.01, 04.04.01) using Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class MoodFollowingTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;
    private String testUsername = "test";
    private String followeeUsername = "Test1"; //test follows test1
    private FirestoreIdlingResource idlingResource;

    /**
     * Sets up test environment by seeding data and launching FollowingFragment.
     */
    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();

        // Register IdlingResource for Firestore
        idlingResource = new FirestoreIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        // Launch MainActivity with HomeFragment and pass username
        activityRule.getScenario().onActivity(activity -> {
            FollowingFragment fragment = new FollowingFragment();
            Bundle args = new Bundle();
            args.putString("username", testUsername);
            fragment.setArguments(args);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment) // Adjust to your container ID
                    .commitAllowingStateLoss();
        });

        // Seed test data to add mood event under followee account
        seedTestData();

        // Wait for Firestore data to load via IdlingResource
        idlingResource.waitForIdle();
    }

    /**
     * Cleans up test data and unregisters IdlingResource.
     */
    @After
    public void tearDown() {
        // Clean up test data
        db.collection("Mood Events")
                .whereEqualTo("username", followeeUsername)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                });

        // Unregister IdlingResource
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    @Test
    public void testPublicMoodEventsfromFollower() {
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    /**
     * Seeds test data into Firestore for the followee user.
     */
    private void seedTestData() {
        // Event posted by the followee as a public event
        MoodEvent followeeMoodEvent = new MoodEvent("Happy", "Almost done this test", "Alone", Timestamp.now(), null, null, followeeUsername, true);
        // Add event to Firestore
        db.collection("Mood Events").document("testFollowEvent").set(followeeMoodEvent);
    }

    /**
     * Custom IdlingResource to wait for Firestore operations.
     */
    private class FirestoreIdlingResource implements IdlingResource {
        private volatile boolean isIdle = false;
        private ResourceCallback callback;

        /** @return Resource name */
        @Override
        public String getName() {
            return "FirestoreIdlingResource";
        }

        /** @return Whether resource is idle */
        @Override
        public boolean isIdleNow() {
            return isIdle;
        }

        /**
         * Registers callback for idle state transition.
         * @param callback Callback to notify when idle
         */
        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.callback = callback;
        }

        /** Waits for Firestore data to load */
        public void waitForIdle() {
            db.collection("Mood Events")
                    .whereEqualTo("username", testUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        isIdle = true;
                        if (callback != null) {
                            callback.onTransitionToIdle();
                        }
                    });
        }
    }
}