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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SignUpTest {
    @Rule
    public ActivityScenarioRule<SignUp> scenario = new
            ActivityScenarioRule<SignUp>(SignUp.class);
    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }
    @Before
    public void seedDatabase() throws InterruptedException {
        // Seed Firestore with a test user document
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> user = new HashMap<>();
        user.put("username", "TestU");
        user.put("password", "TestP");
        user.put("email","test@gmail.com");
        user.put("mobile","12345678");
        usersRef.document().set(user);

        Thread.sleep(1000);
    }
    @Test
    public void validSignUpNavigatesToLogin() throws InterruptedException {
        // Enter valid credentials and click SignUp
        onView(withId(R.id.SUsername)).perform(replaceText("NewUser"));
        onView(withId(R.id.SEmail)).perform(replaceText("new@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("NewPass"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Verify that Login is displayed by checking for a key view
        onView(withId(R.id.button_login)).check(matches(isDisplayed()));
    }
    @Test
    public void emptyUsernameShowsError() throws InterruptedException {
        // Leave username empty, fill password, and click login
        onView(withId(R.id.SUsername)).perform(replaceText(""));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Check that the username field shows the error
        onView(withId(R.id.SUsername)).check(matches(hasErrorText("Username cannot be empty!")));
    }
    @Test
    public void emptyEmailShowsError() throws InterruptedException {
        // Leave email empty, fill password, and click login
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText(""));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Check that the email field shows the error
        onView(withId(R.id.SEmail)).check(matches(hasErrorText("Email cannot be empty!")));
    }
    @Test
    public void emptyMobileShowsError() throws InterruptedException {
        // Leave username empty, fill password, and click login
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText(""));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Check that the username field shows the error
        onView(withId(R.id.SMobile)).check(matches(hasErrorText("Mobile number cannot be empty!")));
    }
    @Test
    public void emptyPasswordShowsError() throws InterruptedException {
        // Leave username empty, fill password, and click login
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText(""));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Check that the username field shows the error
        onView(withId(R.id.SPassword)).check(matches(hasErrorText("Password cannot be empty!")));
    }
    @Test
    public void duplicateUsernameShowsError() throws InterruptedException {
        // Leave username empty, fill password, and click login
        onView(withId(R.id.SUsername)).perform(replaceText("TestU"));
        onView(withId(R.id.SEmail)).perform(replaceText("test@gmail.com"));
        onView(withId(R.id.SMobile)).perform(replaceText("12345678"));
        onView(withId(R.id.SPassword)).perform(replaceText("TestP"));
        onView(withId(R.id.btnCreateAccount)).perform(click());
        Thread.sleep(1000);
        // Check that the username field shows the error
        onView(withId(R.id.SUsername)).check(matches(hasErrorText("Username already exists. Please Choose a different Username")));
    }
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
