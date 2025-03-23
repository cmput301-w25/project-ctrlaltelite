package com.example.ctrlaltelite;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
import static org.hamcrest.Matchers.anything;

/**
 * UI tests for user search and profile viewing functionalities (US 03.02.01, 03.03.01) using Espresso.
 */
@RunWith(AndroidJUnit4.class)
public class SearchAndProfileTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;
    private String testCurrentUserUsername = "currentTestUser";
    private FirestoreIdlingResource idlingResource;

    /**
     * Sets up test environment by seeding data and launching SearchFragment.
     */
    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();

        // Seed test data
        seedTestData();

        // Register IdlingResource for Firestore
        idlingResource = new FirestoreIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        // Launch MainActivity with SearchFragment and pass current user username
        activityRule.getScenario().onActivity(activity -> {
            SearchFragment fragment = new SearchFragment(testCurrentUserUsername);
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
        db.collection("users")
                .whereEqualTo("username", testCurrentUserUsername)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (var doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                });
        db.collection("users")
                .whereEqualTo("username", "otherTestUser")
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
     * Tests searching for other users by typing a partial username (US 03.02.01).
     */
    @Test
    public void testSearchForOtherUsers() {
        // Ensure the user list is initially visible
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));

        // Type "other" into the search input
        onView(withId(R.id.search_user_input)).perform(typeText("other"));

        // Verify that the user list is still displayed (should show only "otherTestUser")
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));
    }

    /**
     * Tests viewing another user's profile by clicking a list item (US 03.03.01).
     */
    @Test
    public void testViewOtherUserProfile() {
        // Ensure the user list is initially visible
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));

        // Click the first item in the user list
        onData(anything())
                .inAdapterView(withId(R.id.user_list))
                .atPosition(0)
                .perform(click());

        // Verify that the OtherUserProfileFragment is displayed (check a unique view ID)
        // Adjust R.id.profile_username to an actual ID from your OtherUserProfileFragment layout
        onView(withId(R.id.username)).check(matches(isDisplayed()));
    }

    /**
     * Seeds test data into Firestore for the test users.
     */
    private void seedTestData() {
        // Current user
        Map<String, Object> currentUser = new HashMap<>();
        currentUser.put("displayName", "Current Test User");
        currentUser.put("username", testCurrentUserUsername);
        currentUser.put("email", "current@test.com");
        currentUser.put("mobile", "1234567890");
        currentUser.put("profilePhotoUrl", null);

        // Other user to search for
        Map<String, Object> otherUser = new HashMap<>();
        otherUser.put("displayName", "Other Test User");
        otherUser.put("username", "otherTestUser");
        otherUser.put("email", "other@test.com");
        otherUser.put("mobile", "0987654321");
        otherUser.put("profilePhotoUrl", null);

        // Add users to Firestore
        db.collection("users").document(testCurrentUserUsername).set(currentUser);
        db.collection("users").document("otherTestUser").set(otherUser);
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
            db.collection("users")
                    .get() // Fetch all users to ensure data is loaded
                    .addOnCompleteListener(task -> {
                        isIdle = true;
                        if (callback != null) {
                            callback.onTransitionToIdle();
                        }
                    });
        }
    }
}
