package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * UI Tests for all functionality regarding the follow request system
 */

@RunWith(AndroidJUnit4.class)
public class FollowRequestsTest {
    @Rule
    public ActivityScenarioRule<Login> activityRule = new ActivityScenarioRule<>(Login.class);

    /**
     * Setting up our instance of the db
     */
    @BeforeClass
    public static void setup() {

        // Specific address for emulated device to access localhost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    /**
     * Adding test data
     */
    @Before
    public void seedDatabase() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference usersRef = db.collection("users");
        Map<String, Object> requester = new HashMap<>();
        requester.put("username", "testUsername1");
        requester.put("displayName", "testUsername1");
        requester.put("password", "testPassword1");
        requester.put("email", "test1@gmail.com");
        requester.put("mobile", "1234567890");
        usersRef.document("testUserDoc1").set(requester);
        // Allowing for some time for the data to be added into the database
        Thread.sleep(1000);

        Map<String, Object> requestee = new HashMap<>();
        requestee.put("username", "testUsername2");
        requestee.put("displayName", "testUsername2");
        requestee.put("password", "testPassword2");
        requestee.put("email", "test2@gmail.com");
        requestee.put("mobile", "123456891");
        usersRef.document("testUserDoc2").set(requestee);
        Thread.sleep(1000);

        Map<String, Object> extraTestUser1 = new HashMap<>();
        extraTestUser1.put("username", "testUsername3");
        extraTestUser1.put("displayName", "testUsername3");
        extraTestUser1.put("password", "testPassword3");
        extraTestUser1.put("email", "test3@gmail.com");
        extraTestUser1.put("mobile", "123456892");
        usersRef.document("testUserDoc3").set(extraTestUser1);
        Thread.sleep(1000);

        Map<String, Object> extraTestUser2 = new HashMap<>();
        extraTestUser2.put("username", "testUsername4");
        extraTestUser2.put("displayName", "testUsername4");
        extraTestUser2.put("password", "testPassword4");
        extraTestUser2.put("email", "test4@gmail.com");
        extraTestUser2.put("mobile", "123456893");
        usersRef.document("testUserDoc4").set(extraTestUser2);
        Thread.sleep(1000);


        CollectionReference followRequestsRef = db.collection("FollowRequests");
        Map<String, Object> testFollowRequest1 = new HashMap<>();
        testFollowRequest1.put("Requestee's Display Name", "testUsername3");
        testFollowRequest1.put("Requestee's Username", "testUsername3");
        testFollowRequest1.put("Requester's Display Name", "testUsername1");
        testFollowRequest1.put("Requester's Username", "testUsername1");
        testFollowRequest1.put("Status", "Pending");
        followRequestsRef.document("testFollowRequest1").set(testFollowRequest1);

        Map<String, Object> testFollowRequest2 = new HashMap<>();
        testFollowRequest2.put("Requestee's Display Name", "testUsername4");
        testFollowRequest2.put("Requestee's Username", "testUsername4");
        testFollowRequest2.put("Requester's Display Name", "testUsername1");
        testFollowRequest2.put("Requester's Username", "testUsername1");
        testFollowRequest2.put("Status", "Accepted");
        followRequestsRef.document("testFollowRequest2").set(testFollowRequest2);

    }

    /**
     * UI test to see if you're able to request to follow a new user
     * (i.e. a user you havent interacted with before in terms of requesting to follow)
     * @throws InterruptedException
     */
    @Test
    public void followRequestingNewUserForFirstTimeShouldBeValid() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername1"));
        onView(withId(R.id.password)).perform(replaceText("testPassword1"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.search)).perform(click());
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));
        onView(withId(R.id.search_user_input)).perform(typeText("testUsername2"));

        onData(anything())
                .inAdapterView(withId(R.id.user_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.follow_button))
                .check(matches(allOf(
                        isDisplayed(),
                        isClickable(),
                        isEnabled()
                )));
        Thread.sleep(1000);

        onView(withId(R.id.follow_button)).check(matches(withText(containsString("ollow"))));
        onView(withId(R.id.follow_button)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.follow_button)).check(matches(withText(containsString("equested"))));

    }

    /**
     * UI test to check if you're unable to make multiple requests to the same person
     * @throws InterruptedIOException
     */
    @Test
    public void followRequestingUserYouAlreadyRequestedShouldNotBePossible() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername1"));
        onView(withId(R.id.password)).perform(replaceText("testPassword1"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);


        onView(withId(R.id.search)).perform(click());
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));
        onView(withId(R.id.search_user_input)).perform(typeText("testUsername3"));

        onData(anything())
                .inAdapterView(withId(R.id.user_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.follow_button))
                .check(matches(allOf(
                        isDisplayed(),
                        isClickable(),
                        isEnabled()
                )));

        Thread.sleep(1000);
        onView(withId(R.id.follow_button)).check(matches(withText(containsString("equested"))));

        // Follow Button should now do nothing
        onView(withId(R.id.follow_button)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.follow_button)).check(matches(withText(containsString("equested"))));
    }

    /**
     * UI test to confirm that when a user has already accepted a request, if will say following on the requester's app
     * @throws InterruptedIOException
     */
    @Test
    public void ShouldSayFollowingOnOtherUserPageIfTheyAlreadyAcceptedYourRequest() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername1"));
        onView(withId(R.id.password)).perform(replaceText("testPassword1"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);


        onView(withId(R.id.search)).perform(click());
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));
        onView(withId(R.id.search_user_input)).perform(typeText("testUsername4"));

        onData(anything())
                .inAdapterView(withId(R.id.user_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.follow_button))
                .check(matches(allOf(
                        isDisplayed(),
                        isClickable(),
                        isEnabled()
                )));

        Thread.sleep(1000);
        onView(withId(R.id.follow_button)).check(matches(withText(containsString("ollowing"))));

        // Follow Button should now do nothing
        onView(withId(R.id.follow_button)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.follow_button)).check(matches(withText(containsString("ollowing"))));

    }
    /**
     * UI test to confirm that when a user accepts a follow request, the other user who requested to follow
     them can see the "following" button
     * @throws InterruptedIOException
     */
    @Test
    public void WhenUserAcceptsRequestOtherUserCanSeeIt() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername3"));
        onView(withId(R.id.password)).perform(replaceText("testPassword3"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.notif)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.accept_button)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.main)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.buttonDrawerToggle)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.logout)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.username)).perform(replaceText("testUsername1"));
        onView(withId(R.id.password)).perform(replaceText("testPassword1"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.search)).perform(click());
        onView(withId(R.id.user_list)).check(matches(isDisplayed()));
        onView(withId(R.id.search_user_input)).perform(typeText("testUsername3"));

        onData(anything())
                .inAdapterView(withId(R.id.user_list))
                .atPosition(0)
                .perform(click());

        onView(withId(R.id.follow_button))
                .check(matches(allOf(
                        isDisplayed(),
                        isClickable(),
                        isEnabled()
                )));

        Thread.sleep(1000);
        onView(withId(R.id.follow_button)).check(matches(withText(containsString("ollowing"))));

        // Follow Button should now do nothing
        onView(withId(R.id.follow_button)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.follow_button)).check(matches(withText(containsString("ollowing"))));

    }

    /**
     * UI test to confirm that when a user rejects a follow request, the request is removed from the follow requests list
     * @throws InterruptedIOException
     */
    @Test
    public void UserShouldBeAbleToRejectFollowRequest() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername3"));
        onView(withId(R.id.password)).perform(replaceText("testPassword3"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.notif)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.follow_request_list)).check(matches(hasChildCount(1)));

        onView(withId(R.id.reject_button)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.follow_request_list)).check(matches(hasChildCount(0)));
    }
    /**
     * Cleans up the seeded documents from the Firestore emulator after each test.
     */
    @After
    public void tearDown() {

        String projectId = "ctrlaltelite-be29f";
        URL url = null;

        try {
            // Construct the URL to delete the specific test document.
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId +
                    "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}