package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.Matchers.is;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class to check conditions for the textual reason - US 02.01
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TextualReasonTest {

    private String testUsername = "testUser";

    // Launch MainActivity with an Intent
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class)
                    .putExtra("username", testUsername));

    private FirebaseFirestore db;
    private FirestoreIdlingResource idlingResource;

    @Before
    public void setUp() throws InterruptedException {
        db = FirebaseFirestore.getInstance();

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

    /**
     * Cleaning up the database after each test
     */
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
     * New Mood Event Should be created upon valid textual reason
     */
    @Test
    public void ValidTextualReasonShouldPass() throws InterruptedException {
        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Reason
        onView(withId(R.id.edit_reason)).perform(typeText("Feeling alone"));

        // Adding a Social Situation
        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Saving the data
        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        // Verify that Mood Event has been created
        onData(Matchers.anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());
        onView(withId(R.id.edit_social_situation_spinner)).check(matches(withSpinnerText("\uD83C\uDFE1 Alone")));
        onView(withId(R.id.edit_reason_edittext)).check(matches(withText("Feeling alone")));
        onView(withId(R.id.edit_mood_spinner)).check(matches(withSpinnerText("\uD83D\uDE0A Happy")));

    }

    /**
     * New Mood Event Should Not be Created Upon Empty Reason Being Enter
     */
    @Test
    public void InvalidTextualReasonDueToEmptyReasonShouldFail() throws InterruptedException {

        onView(withId(R.id.add)).perform(click());

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.edit_reason)).perform(typeText(""));

        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.edit_reason)).check(matches(hasErrorText("Either a reason or an image must be provided")));
    }

    /**
     * Mood event should be created upon valid social situation
     * @throws InterruptedException
     */
    @Test
    public void ValidMoodShouldPass() throws InterruptedException {

        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Reason
        onView(withId(R.id.edit_reason)).perform(typeText("Feeling alone"));

        // Adding a Social Situation
        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Saving the data
        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        // Verify that Mood Event has been created
        onData(Matchers.anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.edit_mood_spinner)).check(matches(withSpinnerText("\uD83D\uDE0A Happy")));

    }

    /**
     * Mood Event should be created upon valid social situation
     * @throws InterruptedException
     */
    @Test
    public void ValidSocialSituationShouldPass() throws InterruptedException {

        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Reason
        onView(withId(R.id.edit_reason)).perform(typeText("Feeling alone"));

        // Adding a Social Situation
        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Saving the data
        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        // Verify that Mood Event has been created
        onData(Matchers.anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.edit_social_situation_spinner)).check(matches(withSpinnerText("\uD83C\uDFE1 Alone")));
    }

    /**
     * Mood cannot be default option (will be done for project part 4)
     * @throws InterruptedException
    */
    @Test
    public void InvalidMoodShouldFail() throws InterruptedException {

        onView(withId(R.id.add)).perform(click());

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(0).perform(click());

        onView(withId(R.id.edit_reason)).perform(typeText(""));

        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.button_cancel)).perform(click());

        onView(withId(R.id.mood_list))
                .check(matches(hasChildCount(0)));
    }
    /**
     * Custom idlingResource to wait for Firestore operations
     */
    private class FirestoreIdlingResource implements IdlingResource {
        private volatile boolean isIdle = false;
        private ResourceCallback callback;

        /**
         * Resource name
         * @return resource name
         */
        @Override
        public String getName() {
            return "FirestoreIdlingResource";
        }

        /**
         * Checking to see if db is idle now
         * @return boolean which checks to see if db is idle now
         */
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

        /**
         * Waiting for the database to load
         */
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
