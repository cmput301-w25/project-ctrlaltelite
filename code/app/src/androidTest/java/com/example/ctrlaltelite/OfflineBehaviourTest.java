package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * UI tests for verifying offline behavior in AddFragment.
 * OfflineAddFragment is a helper subclass that forces offline state by overriding isNetworkAvailable() to always return false.
 * These tests verify that:
 * • Adding a mood event offline displays the event’s reason in the list.
 * • Editing a mood event offline updates the displayed reason.
 * • Deleting a mood event offline removes it from the list.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OfflineBehaviourTest {

    // Launch MainActivity with an Intent
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .putExtra("username", "TestOfflineUser"));

    /**
     * OfflineAddFragment is a helper subclass of AddFragment that forces offline state.
     */
    public static class OfflineAddFragment extends AddFragment {
        @Override
        protected boolean isNetworkAvailable() {
            // Force offline state
            return false;
        }
    }

    /**
     * Cleaning up database after each test
     * @throws InterruptedException
     */
    @After
    public void tearDown() throws InterruptedException {
        // Delete mood events for the test user ("TestOfflineUser") from Firestore.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        db.collection("Mood Events")
                .whereEqualTo("username", "TestOfflineUser")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            doc.getReference().delete();
                        }
                    }
                    latch.countDown();
                });
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * Test that adding a mood event while offline works correctly.
     * It selects a valid mood ("Happy") and enters the reason "Offline test event",
     * then verifies that the newly added mood event is displayed on the screen.
     */
    @Test
    public void testOfflineMoodEventAddition() throws InterruptedException {
        // Navigate to AddFragment via the bottom navigation "add" button.
        onView(withId(R.id.add)).perform(click());

        // Replace the current AddFragment with OfflineAddFragment.
        activityRule.getScenario().onActivity(activity -> {
            OfflineAddFragment offlineFragment = new OfflineAddFragment();
            Bundle args = new Bundle();
            args.putString("username", "TestOfflineUser");
            offlineFragment.setArguments(args);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, offlineFragment)
                    .commitNow();
        });

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(allOf(anything(), containsString("Happy"))).perform(click());

        onView(withId(R.id.edit_reason)).perform(replaceText("Offline test event"));

        onView(withId(R.id.button_save)).perform(click());

        Thread.sleep(2000);

        // Verify that the mood event with the reason "Offline test event" is displayed.
        onView(withText("Offline test event")).check(matches(isDisplayed()));
    }

    /**
     * Test that editing a mood event while offline updates the event's details.
     * This test first adds a mood event offline with reason "Original offline event",
     * then opens the edit dialog, changes the reason to "Edited offline event",
     * saves, and verifies that the updated event is displayed.
     */
    @Test
    public void testOfflineMoodEventEditing() throws InterruptedException {
        // First, add a mood event offline with reason "Original offline event".
        onView(withId(R.id.add)).perform(click());
        activityRule.getScenario().onActivity(activity -> {
            OfflineAddFragment offlineFragment = new OfflineAddFragment();
            Bundle args = new Bundle();
            args.putString("username", "TestOfflineUser");
            offlineFragment.setArguments(args);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, offlineFragment)
                    .commitNow();
        });
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(allOf(anything(), containsString("Happy"))).perform(click());
        onView(withId(R.id.edit_reason)).perform(replaceText("Original offline event"));
        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(2000); // Wait for the event to appear in the list

        // Click the first item in the mood list to open the edit dialog.
        onData(anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        // In the edit dialog, change the reason to "Edited offline event".
        onView(withId(R.id.edit_reason_edittext)).perform(clearText(), replaceText("Edited offline event"));

        onView(withId(R.id.save_button)).perform(click());
        Thread.sleep(2000); // Wait for update

        // Verify that the mood event now displays the updated reason.
        onView(withText("Edited offline event")).check(matches(isDisplayed()));
    }

    /**
     * Test that deleting a mood event while offline removes it from the list.
     * This test first adds a mood event offline with reason "Event to delete",
     * then opens the edit dialog, clicks the delete button, and verifies that the event is no longer displayed.
     */
    @Test
    public void testOfflineMoodEventDeletion() throws InterruptedException {
        // First, add a mood event offline with reason "Event to delete".
        onView(withId(R.id.add)).perform(click());
        activityRule.getScenario().onActivity(activity -> {
            OfflineAddFragment offlineFragment = new OfflineAddFragment();
            Bundle args = new Bundle();
            args.putString("username", "TestOfflineUser");
            offlineFragment.setArguments(args);
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, offlineFragment)
                    .commitNow();
        });
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(allOf(anything(), containsString("Happy"))).perform(click());
        onView(withId(R.id.edit_reason)).perform(replaceText("Event to delete"));
        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(2000);

        // Click the first item in the mood list to open the edit dialog.
        onData(anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        // Click the delete button in the edit dialog.
        onView(withId(R.id.delete_button)).perform(click());
        Thread.sleep(2000);

        // Verify that the mood event with the reason "Event to delete" is no longer displayed.
        onView(withText("Event to delete")).check(doesNotExist());
    }
}
