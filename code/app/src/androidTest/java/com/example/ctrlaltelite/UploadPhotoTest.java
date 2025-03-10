package com.example.ctrlaltelite;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

import android.Manifest;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
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
 * UI tests for mood event reason why visual explanation (US 02.02.01 and US 02.02.02)
 */
/*
@RunWith(AndroidJUnit4.class)
public class UploadPhotoTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(Manifest.permission.READ_MEDIA_IMAGES);
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;
    private String testUsername = "test";
    private FirestoreIdlingResource idlingResource;

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        db = FirebaseFirestore.getInstance();

        // Seed test data
        seedTestData();

        // Register IdlingResource for Firestore
        idlingResource = new FirestoreIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        // Launch MainActivity with HomeFragment and pass username
        activityRule.getScenario().onActivity(activity -> {
            AddFragment fragment = new AddFragment();
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
        Intents.release();
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

    @Test
    public void testUploadPhoto() {
        //What is expected when photo picker
        Matcher<Intent> expectedIntent = allOf(
                hasAction(MediaStore.ACTION_PICK_IMAGES)
        );
        Instrumentation.ActivityResult activityResult = createGalleryPickStub();
        intending(expectedIntent).respondWith(activityResult);

        //Uploaded image should not be shown
        onView(withId(R.id.uploaded_image)).check(matches(not(isDisplayed())));

        // Click the upload button
        onView(withId(R.id.button_upload)).perform(click());
        intended(expectedIntent);
        //Uploaded image should be shown
        onView(withId(R.id.uploaded_image)).check(matches(isDisplayed()));

    }

    private Instrumentation.ActivityResult createGalleryPickStub() {
        Uri testUri = Uri.parse("android.resource://com.example.ctrlaltelite/drawable/logo1");
        Intent resultIntent = new Intent(MediaStore.ACTION_PICK_IMAGES, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultIntent.putExtra("1", testUri);
        return new Instrumentation.ActivityResult(RESULT_OK, resultIntent);
    }



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
*/