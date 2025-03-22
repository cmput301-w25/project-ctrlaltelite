package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

import android.os.Bundle;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI tests for mood event viewing and editing functionalities US 01.04.01 and 01.05.01 using Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class MoodEventViewDeleteEditUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;
    private String testUsername = "testUser";
    private FirestoreIdlingResource idlingResource;

    @Before
    public void setUp() throws InterruptedException {
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
                    .replace(R.id.frameLayout, fragment)
                    .commitAllowingStateLoss();
        });

        // Wait for Firestore data to load via IdlingResource
        idlingResource.waitForIdle();
    }

    @After
    public void tearDown() {
        // Clean up test data
        db.collection("Mood Events")
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
     * Tests the viewing of a mood event's details in the UI. US 01.04.01
     * <p>
     * Simulates clicking the first item in the mood list to open the edit dialog and verifies
     * that the dialog displays the expected mood event details seeded in {@link #seedTestData()}.
     */
    @Test
    public void testMoodEventViewing_DisplayDetails() {
        // Click the first item in the mood list to open the edit dialog
        onData(anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        // Verify dialog fields are displayed with expected data
        onView(withId(R.id.edit_mood_spinner)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_reason_edittext)).check(matches(withText("Good day")));
        onView(withId(R.id.edit_social_situation_spinner)).check(matches(isDisplayed()));
    }

    /**
     * Tests the editing of a mood event's details in the UI. US 01.05.01
     * <p>
     * Simulates editing the "reason" field of a mood event, saving the changes, and verifying
     * that the updated value is reflected when the dialog is reopened.
     */
    @Test
    public void testMoodEventEditing_UpdateDetails() {
        // Click the first item to open the edit dialog
        onData(anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        // Update the reason field
        onView(withId(R.id.edit_reason_edittext)).perform(replaceText("Great day"));

        // Save changes
        onView(withId(R.id.save_button)).perform(click());

        // Wait for Firestore update
        idlingResource.waitForIdle();

        // Reopen the dialog and verify the updated value
        onData(anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());
        onView(withId(R.id.edit_reason_edittext)).check(matches(withText("Great day")));
    }

    /**
     * Checking to see if a mood event is deleted upon pressing the delete button - US 01.06
     */
    @Test
    public void DeleteMoodEventIsSuccessful() {

        // Click the first item in the mood list to open the edit dialog
        onData(CoreMatchers.anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        // Submit form
        onView(withId(R.id.delete_button)).perform(click());

        // Wait for Firestore update
        idlingResource.waitForIdle();

        // Check that there is nothing in the display view now
        onView(withId(R.id.mood_list)).check(matches(hasChildCount(0)));
    }

    /**
     * Adding test data
     * @throws InterruptedException
     */
    private void seedTestData() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Map<String, Object> moodEvent = new HashMap<>();
        moodEvent.put("username", testUsername);
        moodEvent.put("emotionalState", "Happy");
        moodEvent.put("reason", "Good day");
        moodEvent.put("trigger", "Friend");
        moodEvent.put("socialSituation", "With Friends");
        moodEvent.put("timestamp", "2025-03-07 12:00:00");
        moodEvent.put("imgPath", null);

        db.collection("Mood Events")
                .add(moodEvent)
                .addOnSuccessListener(docRef -> latch.countDown())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to seed test data", e);
                });

        // Wait for data to be added (timeout after 5 seconds)
        latch.await(5, TimeUnit.SECONDS);
    }

    // Custom IdlingResource to wait for Firestore operations
    private class FirestoreIdlingResource implements IdlingResource {
        private volatile boolean isIdle = false;
        private ResourceCallback callback;

        @Override
        public String getName() {
            return "FirestoreIdlingResource";
        }

        @Override
        public boolean isIdleNow() {
            return isIdle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.callback = callback;
        }

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