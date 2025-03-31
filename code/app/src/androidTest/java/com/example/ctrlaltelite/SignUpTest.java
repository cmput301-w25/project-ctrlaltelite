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
 * UI tests for the SignUp activity using Espresso.
 * <p>
 * This test class seeds the Firestore emulator with a test user before each test and verifies that
 * input validations and navigation behave as expected when signing up.
 * It cleans up the Firestore documents after the tests.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpTest {

    /**
     * ActivityScenarioRule to launch the SignUp activity for testing.
     */
    @Rule
    public ActivityScenarioRule<SignUp> scenario = new ActivityScenarioRule<>(SignUp.class);

    /**
     * Configures the Firestore emulator before any tests run.
     */
    @BeforeClass
    public static void setup(){
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    /**
     * Seeds the Firestore emulator with a test user document before each test.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Before
    public void seedDatabase() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> user = new HashMap<>();
        user.put("username", "TestU");
        user.put("password", "TestP");
        user.put("email", "test@gmail.com");
        user.put("mobile", "12345678");
        usersRef.document().set(user);
        Thread.sleep(1000);
    }

    /**
     * Tests that entering valid sign-up credentials navigates the user to the Login screen.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void validSignUpNavigatesToLogin() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("NewUser"));
        onView(withId(R.id.SDisplayName)).perform((replaceText("NewUser")));
        onView(withId(R.id.SEmail)).perform(replaceText("new@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("1234567890"));
        onView(withId(R.id.SPassword)).perform(replaceText("NewPassword1"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Verify that the Login activity is displayed
        onView(withId(R.id.button_login)).check(matches(isDisplayed()));
    }

    /**
     * Tests that leaving the username field empty shows an error.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void emptyUsernameShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText(""));
        onView(withId(R.id.SDisplayName)).perform((replaceText("NewUser")));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Check that the username field shows the error
        onView(withId(R.id.SUsername)).check(matches(hasErrorText("Username cannot be empty!")));
    }

    /**
     * Tests that leaving the email field empty shows an error.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void emptyEmailShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform((replaceText("NewUser")));
        onView(withId(R.id.SEmail)).perform(replaceText(""));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SEmail)).check(matches(hasErrorText("Email cannot be empty!")));
    }

    /**
     * Tests that leaving the mobile number field empty shows an error.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void emptyMobileShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform((replaceText("NewUser")));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText(""));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SMobile)).check(matches(hasErrorText("Mobile number cannot be empty!")));
    }

    /**
     * Tests that leaving the password field empty shows an error.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void emptyPasswordShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform((replaceText("NewUser")));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText(""));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SPassword)).check(matches(hasErrorText("Password cannot be empty!")));
    }

    /**
     * Tests that attempting to sign up with a duplicate username shows an error.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void duplicateUsernameShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("1234567890"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestPassword1"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SUsername)).check(matches(hasErrorText("Username already exists. Please Choose a different Username")));
    }

    /**
     * Cleans up the seeded documents from the Firestore emulator after each test.
     */

    @Test
    public void emptyDisplayNameShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SDisplayName)).check(matches(hasErrorText("Display name cannot be empty!")));
    }

    /**
     * Checking to see if a non 10 digit mobile number will be invalidated
     * @throws InterruptedException
     */
    @Test
    public void nonTenDigitMobileNumberShouldBeInvalidated() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SMobile)).check(matches(hasErrorText("Phone number must be 10 digits long")));
    }

    /**
     * Check to see if an invalid email will thrown an error
     * @throws InterruptedException
     */

    @Test
    public void InvalidEmailShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("testgmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("1234567890"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SEmail)).check(matches(hasErrorText("Please enter a valid email address")));
    }

    /**
     * Check to see if an invalid password will throw an error
     * @throws InterruptedException
     */
    @Test
    public void InvalidPasswordShowsError() throws InterruptedException {
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SDisplayName)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("1234567890"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.SPassword)).check(matches(hasErrorText("Invalid Password, Must contain:\n" +
                "- At least one letter\n" +
                "- At least one digit\n" +
                "- Minimum 8 characters")));
    }

    /**
     * Cleaning up database after each test
     */
    @After
    public void tearDown() {
        String projectId = "ctrlaltelite-be29f";
        URL url = null;
        try {
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
