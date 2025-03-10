package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
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
        Map<String, Object> user = new HashMap<>();
        user.put("username", "testUsername");
        user.put("password", "testPassword");
        user.put("email", "test@gmail.com");
        user.put("mobile", "4567843");
        usersRef.document("testUserDoc").set(user);
        // Allowing for some time for the data to be added into the database
        Thread.sleep(1000);

        // Additional test data that is not needed as of right now
        /*
        CollectionReference moodEventsRef = db.collection("Mood Events");
        Map<String, Object> moodEvent = new HashMap<>();
        moodEvent.put("username", "testUsername");
        moodEvent.put("emotionalState", "Happy");
        moodEvent.put("reason", "Good day");
        moodEvent.put("trigger", "Friend");
        moodEvent.put("socialSituation", "With Friends");
        moodEvent.put("formattedtimestamp", "2025-03-07 12:00:00");
        moodEvent.put("imgPath", null);
        moodEventsRef.document().set(moodEvent);

        // Allowing for some time for the data to be added into the database
        Thread.sleep(1000);
         */
    }

    /**
     * New Mood Event Should be created upon valid textual reason
     */
    @Test
    public void ValidTextualReasonShouldPass() throws InterruptedException {

        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Trigger
        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

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
        onView(withId(R.id.edit_mood_spinner)).check(matches(withText("Happy")));
        onView(withId(R.id.edit_reason_edittext)).check(matches(withText("Feeling alone")));
        onView(withId(R.id.edit_trigger)).check(matches(withText("Sitting alone at school")));
        onView(withId(R.id.edit_social_situation_spinner)).check(matches(withText("Alone")));

    }

    /**
     * New Mood Event Should Not be Created Upon Entering A Textual Reason Greater than 4 Words
     */
    @Test
    public void InvalidTextualReasonDueToTooManyWordsShouldFail() throws InterruptedException {

        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        // Same Situation as previous test (and the same situation that occurs for the next two tests)
        onView(withId(R.id.add)).perform(click());

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

        onView(withId(R.id.edit_reason)).perform(typeText("I Feel A Bit Alone"));

        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        //onView(withText("Reason cannot be more than 3 words")).check(matches(isDisplayed()));
        onView(withId(R.id.edit_reason)).check(matches(hasErrorText("Reason cannot be more than 3 words")));
    }

    /**
     * New Mood Event Should Not be Created Upon Entering A Textual Reason With More Than 20 Characters
     */
    @Test
    public void InvalidTextualReasonDueToTooManyCharactersShouldFail() throws InterruptedException {

        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.add)).perform(click());

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

        onView(withId(R.id.edit_reason)).perform(typeText("Ridiculously Insurmountable Loneliness"));

        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        //onView(withText("Reason cannot have more than 20 characters")).check(matches(isDisplayed()));
        onView(withId(R.id.edit_reason)).check(matches(hasErrorText("Reason cannot have more than 20 characters")));

    }

    /**
     * New Mood Event Should Not be Created Upon Empty Reason Being Enter
     */
    @Test
    public void InvalidTextualReasonDueToEmptyReasonShouldFail() throws InterruptedException {

        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.add)).perform(click());

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

        onView(withId(R.id.edit_reason)).perform(typeText(""));

        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        //onView(withText("Either a reason or an image must be provided")).check(matches(isDisplayed()));
        onView(withId(R.id.edit_reason)).check(matches(hasErrorText("Either a reason or an image must be provided")));
    }

    /**
     * Mood event should be created upon valid social situation
     * @throws InterruptedException
     */
    @Test
    public void ValidMoodShouldPass() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Trigger
        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

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

        onView(withId(R.id.edit_mood_spinner)).check(matches(withText("Happy")));

    }

    /**
     * Mood Event should be created upon valid social situation
     * @throws InterruptedException
     */
    @Test
    public void ValidSocialSituationShouldPass() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Trigger
        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

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

        onView(withId(R.id.edit_social_situation_spinner)).check(matches(withText("Alone")));
    }

    /**
     * Mood Event Should be created upon valid trigger
     * @throws InterruptedException
     */
    @Test
    public void ValidTriggerShouldPass() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        // Reaching the Add Fragment
        onView(withId(R.id.add)).perform(click());

        // Adding a Mood
        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        // Adding a Trigger
        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

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

        onView(withId(R.id.edit_trigger)).check(matches(withText("Sitting alone at school")));
    }

    /**
     * Mood cannot be default option
     * @throws InterruptedException
     */
    @Test
    public void InvalidMoodShouldFail() throws InterruptedException {
        onView(withId(R.id.username)).perform(replaceText("testUsername"));
        onView(withId(R.id.password)).perform(replaceText("testPassword"));
        onView(withId(R.id.button_login)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.add)).perform(click());

        onView(withId(R.id.dropdown_mood)).perform(click());
        onData(anything()).atPosition(0).perform(click());

        onView(withId(R.id.edit_trigger)).perform(typeText("Sitting alone at school"));

        onView(withId(R.id.edit_reason)).perform(typeText(""));

        onView(withId(R.id.social_situation_spinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());

        onView(withId(R.id.button_save)).perform(click());
        Thread.sleep(1000);

        onView(withText("Emotional state cannot be the default option")).check(matches(isDisplayed()));

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
