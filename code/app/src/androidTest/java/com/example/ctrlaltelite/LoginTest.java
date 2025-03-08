package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
 * UI test for the Login activity.
 * <p>
 * This test class seeds a test user into the Firestore emulator before each test and
 * verifies that login errors and navigation behave as expected.
 * It also cleans up the seeded document after tests.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {

    /**
     * ActivityScenarioRule to launch the Login activity for testing.
     */
    @Rule
    public ActivityScenarioRule<Login> scenario = new ActivityScenarioRule<>(Login.class);

    /**
     * Sets up the Firestore emulator before any tests are run.
     */
    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access localhost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    /**
     * Seeds the Firestore emulator with a test user document before each test.
     *
     * @throws InterruptedException if thread sleep is interrupted.
     */
    @Before
    public void seedDatabase() throws InterruptedException {
        // Seed Firestore with a test user document.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> user = new HashMap<>();
        user.put("username", "TestU");
        user.put("password", "TestP");
        user.put("email", "test@gmail.com");
        user.put("mobile", "4567843");
        usersRef.document("testUserDoc").set(user);

        Thread.sleep(1000);
    }

    /**
     * Verifies that entering valid credentials navigates the user to Homepage.
     *
     * @throws InterruptedException if thread sleep is interrupted.
     */
    @Test
    public void validLoginNavigatesToHomepage() throws InterruptedException {
        // Enter valid credentials and click login.
        onView(withId(R.id.username)).perform(replaceText("TestU"));
        onView(withId(R.id.password)).perform(replaceText("TestP"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.btnNav)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that leaving the username field empty shows an error message.
     */
    @Test
    public void emptyUsernameShowsError() {
        // Leave username empty, fill password, and click login.
        onView(withId(R.id.username)).perform(replaceText(""));
        onView(withId(R.id.password)).perform(replaceText("TestP"));
        onView(withId(R.id.button_login)).perform(click());

        // Check that the username field shows the appropriate error.
        onView(withId(R.id.username)).check(matches(hasErrorText("Username cannot be empty!")));
    }

    /**
     * Verifies that leaving the password field empty shows an error message.
     */
    @Test
    public void emptyPasswordShowsError() {
        // Fill username, leave password empty, and click login.
        onView(withId(R.id.username)).perform(replaceText("TestU"));
        onView(withId(R.id.password)).perform(replaceText(""));
        onView(withId(R.id.button_login)).perform(click());

        // Check that the password field shows the appropriate error.
        onView(withId(R.id.password)).check(matches(hasErrorText("Password cannot be empty!")));
    }

    /**
     * Verifies that using an unregistered username shows an error message.
     *
     * @throws InterruptedException if thread sleep is interrupted.
     */
    @Test
    public void unregisteredUsernameShowsError() throws InterruptedException {
        // Enter credentials for a user that does not exist.
        onView(withId(R.id.username)).perform(replaceText("NewU"));
        onView(withId(R.id.password)).perform(replaceText("NewP"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);
        // Check that the username field shows an error indicating the user was not found.
        onView(withId(R.id.username)).check(matches(hasErrorText("User not found")));
    }

    /**
     * Cleans up the seeded test user document from the Firestore emulator after each test.
     */
    @After
    public void tearDown() {
        String projectId = "ctrlaltelite-be29f";
        URL url = null;
        try {
            // Construct the URL to delete the specific test document.
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId +
                    "/databases/(default)/documents/users/testUserDoc");
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
