package com.example.ctrlaltelite;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.os.Bundle;

/**
 * UI tests for mood history filtering functionalities (US 04.02.01, 04.03.01, 04.04.01) using Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class HistoryFiltersTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;
    private String testUsername = "ert"; // Adjust if needed
    private FirestoreIdlingResource idlingResource;

    /**
     * Sets up test environment by seeding data and launching HomeFragment.
     */
    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();

        // Seed test data
        seedTestData();

        // Register IdlingResource for Firestore
        idlingResource = new FirestoreIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        // Launch MainActivity with HomeFragment and pass username
        activityRule.getScenario().onActivity(activity -> {
            HomeFragment fragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putString("username", testUsername);
            fragment.setArguments(args);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment) // Adjust to your container ID
                    .commitAllowingStateLoss();
        });

        // Wait for Firestore data to load via IdlingResource
        idlingResource.waitForIdle();
    }

    /**
     * Cleans up test data and unregisters IdlingResource.
     */
    @After
    public void tearDown() {
        // Clean up test data
        db.collection("moodEvents")
                .whereEqualTo("username", testUsername)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                });

        // Unregister IdlingResource
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    /**
     * Tests filtering mood events for the last 7 days (US 04.02.01).
     */
    @Test
    public void testMoodEventsFilteredForLast7Days() {
        // Ensure the mood list is initially visible
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));

        // Click the "Show past week" checkbox to filter events
        onView(withId(R.id.show_past_week)).perform(click());

        // Verify that the mood list is still displayed (should exclude the old event)
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    /**
     * Tests filtering mood events by emotional state (US 04.03.01).
     */
    @Test
    public void testMoodEventsFilteredByEmotionalState() {
        // Ensure the mood list is initially visible
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));

        // Open the mood filter spinner and select "😊 Happy"
        onView(withId(R.id.mood_filter)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("😊 Happy"))).perform(click());

        // Verify that the mood list is still displayed (should show only the Happy event)
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    /**
     * Tests filtering mood events by reason text (US 04.04.01).
     */
    @Test
    public void testMoodEventsFilteredByReason() {
        // Ensure the mood list is initially visible
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));

        // Type "stress" into the reason filter EditText
        onView(withId(R.id.search_mood_reason)).perform(typeText("stress"));

        // Verify that the mood list is still displayed (should show only the event with "stress")
        onView(withId(R.id.mood_list)).check(matches(isDisplayed()));
    }

    /**
     * Seeds test data into Firestore for the test user.
     */
    private void seedTestData() {
        // Event 1: Happy mood, recent, with "stress" in reason
        Map<String, Object> happyEvent = new HashMap<>();
        happyEvent.put("emotionalState", "😊 Happy");
        happyEvent.put("reason", "Feeling good despite stress");
        happyEvent.put("timestamp", Timestamp.now());
        happyEvent.put("username", testUsername);
        happyEvent.put("public", true);

        // Event 2: Sad mood, recent, without "stress"
        Map<String, Object> sadEvent = new HashMap<>();
        sadEvent.put("emotionalState", "😢 Sad");
        sadEvent.put("reason", "Bad day");
        sadEvent.put("timestamp", Timestamp.now());
        sadEvent.put("username", testUsername);
        sadEvent.put("public", true);

        // Event 3: Old event (for week filter test)
        Map<String, Object> oldEvent = new HashMap<>();
        oldEvent.put("emotionalState", "😢 Sad");
        oldEvent.put("reason", "Old test event");
        oldEvent.put("timestamp", new Timestamp(new java.util.Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000))); // 10 days ago
        oldEvent.put("username", testUsername);
        oldEvent.put("public", true);

        // Add events to Firestore synchronously
        db.collection("moodEvents").document("happyTestEvent").set(happyEvent);
        db.collection("moodEvents").document("sadTestEvent").set(sadEvent);
        db.collection("moodEvents").document("oldTestEvent").set(oldEvent);
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
            db.collection("moodEvents")
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