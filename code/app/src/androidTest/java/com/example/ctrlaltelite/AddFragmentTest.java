package com.example.ctrlaltelite;
import com.example.ctrlaltelite.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.util.Log;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.ctrlaltelite.MainActivity;
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
public class AddFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testAddingMoodEventWithTextReason() {
        // Navigate to Add Mood Event fragment
        Espresso.onView(ViewMatchers.withId(R.id.btnNav)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.add)).perform(ViewActions.click());

        // Select a mood
        Espresso.onView(ViewMatchers.withId(R.id.dropdown_mood)).perform(ViewActions.click());
        Espresso.onData(org.hamcrest.Matchers.anything()).atPosition(1).perform(ViewActions.click());

        // Enter a reason
        Espresso.onView(ViewMatchers.withId(R.id.edit_reason))
                .perform(ViewActions.typeText("Feeling happy today!"), ViewActions.closeSoftKeyboard());

        // Click Save
        Espresso.onView(ViewMatchers.withId(R.id.button_save)).perform(ViewActions.click());

        // Check for success message
        Espresso.onView(ViewMatchers.withText("Mood event saved!"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testAddingMoodEventWithImage() {
        // Navigate to Add Mood Event fragment
        Espresso.onView(ViewMatchers.withId(R.id.btnNav)).perform(ViewActions.click());
        Espresso.onView(ViewMatchers.withId(R.id.add)).perform(ViewActions.click());

        // Select a mood
        Espresso.onView(ViewMatchers.withId(R.id.dropdown_mood)).perform(ViewActions.click());
        Espresso.onData(org.hamcrest.Matchers.anything()).atPosition(1).perform(ViewActions.click());

        // Upload an image (mocked, as Espresso cannot interact with external pickers)
        Espresso.onView(ViewMatchers.withId(R.id.button_upload)).perform(ViewActions.click());

        // Click Save
        Espresso.onView(ViewMatchers.withId(R.id.button_save)).perform(ViewActions.click());

        // Check for success message
        Espresso.onView(ViewMatchers.withText("Mood event saved!"))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
