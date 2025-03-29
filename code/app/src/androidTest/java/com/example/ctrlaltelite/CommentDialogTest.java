package com.example.ctrlaltelite;

import android.os.Bundle;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.anything;

@RunWith(AndroidJUnit4.class)
public class CommentDialogTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;
    private String testCurrentUserUsername = "currentTestUser";
    private String testMoodEventId = "testMoodEvent";
    private FirestoreIdlingResource idlingResource;

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();

        seedTestData();

        idlingResource = new FirestoreIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);
        idlingResource.waitForIdle();

        activityRule.getScenario().onActivity(activity -> {
            HomeFragment fragment = new HomeFragment();
            Bundle args = new Bundle();
            args.putString("username", testCurrentUserUsername);
            fragment.setArguments(args);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .disallowAddToBackStack()
                    .commit();
        });
    }

    @After
    public void tearDown() {
        // Delete comments manually
        db.collection("Mood Events").document(testMoodEventId)
                .collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                });
        db.collection("users").document(testCurrentUserUsername).delete();
        db.collection("Mood Events").document(testMoodEventId).delete();
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    private void seedTestData() {
        Map<String, Object> user = new HashMap<>();
        user.put("displayName", "Current Test User");
        user.put("username", testCurrentUserUsername);
        db.collection("users").document(testCurrentUserUsername).set(user);

        Map<String, Object> mood = new HashMap<>();
        mood.put("username", testCurrentUserUsername);
        mood.put("emotionalState", "😊 Happy");
        mood.put("timestamp", Timestamp.now()); 
        mood.put("documentId", testMoodEventId);
        db.collection("Mood Events").document(testMoodEventId).set(mood);

        Map<String, Object> comment = new HashMap<>();
        comment.put("text", "Initial comment");
        comment.put("username", "Other Test User");
        comment.put("timestamp", Timestamp.now()); 
        db.collection("Mood Events").document(testMoodEventId)
                .collection("comments").add(comment);
    }

    @Test
    public void testOpenCommentDialogAndSubmit() throws InterruptedException {

        onData(anything())
                .inAdapterView(withId(R.id.mood_list))
                .atPosition(0)
                .onChildView(withId(R.id.comments_button))
                .perform(click());

        onView(withId(R.id.comment_input)).check(matches(isDisplayed()));
        onView(withId(R.id.comment_input)).perform(typeText("Test comment"), closeSoftKeyboard());
        onView(withId(R.id.submit_comment_button)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.comment_input)).check(matches(withText("")));
        onView(allOf(withId(R.id.comment_text), withText("Test comment")))
                .check(matches(isDisplayed()));

    }

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
            FirebaseFirestore.getInstance()
                    .collection("Mood Events")
                    .whereEqualTo("username", testCurrentUserUsername)
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
