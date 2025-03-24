package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented tests for US 06.04.01:
 *
 * Each test seeds Firestore with a different scenario, launches the MapNearbyFragment,
 * and does basic Espresso checks to confirm the map container is displayed.
 *
 */
@RunWith(AndroidJUnit4.class)
public class MapNearbyFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;

    private static final String TEST_USERNAME = "MapTestUser";
    private static final String FOLLOWED_USER_A = "FollowedUserA";
    private static final String FOLLOWED_USER_B = "FollowedUserB";

    @Before
    public void setUp() {
        db = FirebaseFirestore.getInstance();
    }

    @After
    public void tearDown() {
        // Clean up any FollowRequests or Mood Events we created
        removeAllSeededData();
    }

    /**
     * Scenario 1:
     * - The user follows 2 participants
     * - A has a mood event in range (~2km), B has a mood event out-of-range (~10km)
     * Expectation: Only A's event shows a marker.
     */
    @Test
    public void testOneInRangeOneOutOfRange() throws InterruptedException {
        seedFollowRequest(TEST_USERNAME, FOLLOWED_USER_A, "Accepted");
        seedFollowRequest(TEST_USERNAME, FOLLOWED_USER_B, "Accepted");

        // A's most recent event ~2km away
        seedMoodEvent(FOLLOWED_USER_A, "😊 Happy", new GeoPoint(53.50, -113.52), Timestamp.now());
        // B's most recent event ~10km away
        seedMoodEvent(FOLLOWED_USER_B, "😢 Sad", new GeoPoint(53.60, -113.52), Timestamp.now());

        launchMapNearbyFragment(TEST_USERNAME);
        Thread.sleep(2000);

        onView(withId(R.id.id_map_nearby)).check(matches(isDisplayed()));

    }

    /**
     * Scenario 2:
     * - The user follows 1 participant
     * - That participant has NO location for their event
     * Expectation: No markers appear.
     */
    @Test
    public void testEventHasNoLocation() throws InterruptedException {
        seedFollowRequest(TEST_USERNAME, FOLLOWED_USER_A, "Accepted");
        // Mood event with location = null
        seedMoodEvent(FOLLOWED_USER_A, "😐 Neutral", null, Timestamp.now());

        launchMapNearbyFragment(TEST_USERNAME);
        Thread.sleep(2000);

        onView(withId(R.id.id_map_nearby)).check(matches(isDisplayed()));
    }

    /**
     * Scenario 3:
     * - The user follows 2 participants
     * - Each has multiple events, but only the most recent event matters
     * - Both have in-range events
     * Expectation: 2 markers appear, for each user’s newest event
     */
    @Test
    public void testMultipleUsersAllInRangeMostRecent() throws InterruptedException {
        seedFollowRequest(TEST_USERNAME, FOLLOWED_USER_A, "Accepted");
        seedFollowRequest(TEST_USERNAME, FOLLOWED_USER_B, "Accepted");

        long oneDayMillis = 24L * 60L * 60L * 1000L;
        long twoDayMillis = 2L * oneDayMillis;

        Date yesterdayDate = new Date(System.currentTimeMillis() - oneDayMillis);
        Date twoDaysDate = new Date(System.currentTimeMillis() - twoDayMillis);
        Timestamp yesterday = new Timestamp(yesterdayDate);
        Timestamp twoDaysAgo = new Timestamp(twoDaysDate);

        // A: older event (1 day ago), newer event (just now)
        seedMoodEvent(FOLLOWED_USER_A, "😢 OldSad", new GeoPoint(53.50, -113.52), yesterday);
        seedMoodEvent(FOLLOWED_USER_A, "😄 NewHappy", new GeoPoint(53.51, -113.52), Timestamp.now());

        // B: older event (2 days ago), new event in-range
        seedMoodEvent(FOLLOWED_USER_B, "😡 OlderAngry", new GeoPoint(53.49, -113.52), twoDaysAgo);
        seedMoodEvent(FOLLOWED_USER_B, "😎 RecentCool", new GeoPoint(53.48, -113.52), Timestamp.now());

        launchMapNearbyFragment(TEST_USERNAME);
        Thread.sleep(2000);

        onView(withId(R.id.id_map_nearby)).check(matches(isDisplayed()));
    }

    /**
     * Scenario 4:
     * - The user does NOT follow anyone (no follow requests accepted)
     * Expectation: no markers.
     */
    @Test
    public void testNoFollowingNoMarkers() throws InterruptedException {
        // Just launch the fragment with an empty seed

        launchMapNearbyFragment(TEST_USERNAME);
        Thread.sleep(2000);

        onView(withId(R.id.id_map_nearby)).check(matches(isDisplayed()));
    }

    // -----------------------------------------------------------------------
    //                       HELPER METHODS
    // -----------------------------------------------------------------------

    /**
     * Launches the MapNearbyFragment by replacing the MainActivity's layout container.
     *
     * @param username - The user logged in
     */
    private void launchMapNearbyFragment(String username) {
        activityScenarioRule.getScenario().onActivity(activity -> {
            MapNearbyFragment fragment = new MapNearbyFragment();
            Bundle args = new Bundle();
            args.putString("username", username);
            fragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, fragment)
                    .commitAllowingStateLoss();
        });
    }

    /**
     * Seeds a FollowRequests document in Firestore with the given status.
     *
     * @param requester The user requesting to follow
     * @param requestee The user being followed
     * @param status    e.g. "Accepted"
     */
    private void seedFollowRequest(String requester, String requestee, String status) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> data = new HashMap<>();
        data.put("Requester's Username", requester);
        data.put("Requestee's Username", requestee);
        data.put("Status", status);

        db.collection("FollowRequests")
                .add(data)
                .addOnSuccessListener(docRef -> latch.countDown())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed to seed follow request", e);
                });

        latch.await(3, TimeUnit.SECONDS);
    }

    /**
     * Seeds a Mood Events doc for a user with optional location.
     *
     * @param username  The user who posted the mood
     * @param emotion   The emotionalState string
     * @param location  A GeoPoint or null
     * @param timestamp A Firestore Timestamp
     */
    private void seedMoodEvent(String username, String emotion, GeoPoint location, Timestamp timestamp)
            throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("emotionalState", emotion);
        data.put("timestamp", timestamp);
        data.put("location", location);
        data.put("imgPath", null);
        data.put("isPublic", true);

        db.collection("Mood Events")
                .add(data)
                .addOnSuccessListener(docRef -> latch.countDown())
                .addOnFailureListener(e -> {
                    throw new RuntimeException("Failed adding mood event", e);
                });

        latch.await(3, TimeUnit.SECONDS);
    }

    /**
     * Removes all seeded follow requests and mood events for our test user(s).
     * This method is called in @After to keep Firestore clean.
     * We do an async call, so no guarantee it finishes before test process ends,
     * but typically it's fine.
     */
    private void removeAllSeededData() {
        // Remove all follow requests where Requester's Username = TEST_USERNAME
        db.collection("FollowRequests")
                .whereEqualTo("Requester's Username", TEST_USERNAME)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> doc.getReference().delete());
                });
        db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", FOLLOWED_USER_A)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> doc.getReference().delete());
                });
        db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", FOLLOWED_USER_B)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> doc.getReference().delete());
                });

        // Remove all mood events
        db.collection("Mood Events")
                .whereEqualTo("username", FOLLOWED_USER_A)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> doc.getReference().delete());
                });
        db.collection("Mood Events")
                .whereEqualTo("username", FOLLOWED_USER_B)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    querySnapshot.forEach(doc -> doc.getReference().delete());
                });
    }
}
