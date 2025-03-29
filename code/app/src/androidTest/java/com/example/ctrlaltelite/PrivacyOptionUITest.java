package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ctrlaltelite.AddFragment;
import com.example.ctrlaltelite.MainActivity;
import com.example.ctrlaltelite.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
//// Test created with guidance from ChatGPT (OpenAI), March 28, 2025
public class PrivacyOptionUITest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void navigateToAddFragment() {
        activityRule.getScenario().onActivity(activity -> {
            AddFragment addFragment = new AddFragment();
            Bundle args = new Bundle();
            args.putString("username", "test_user");
            addFragment.setArguments(args);

            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout, addFragment)
                    .commitNow(); // Use commitNow to block until fragment is added
        });
    }

    //// Test created with guidance from ChatGPT (OpenAI), March 28, 2025
    @Test
    public void testPublicOptionSelected() {
        onView(withId(R.id.radioPublic)).perform(click());
        onView(withId(R.id.radioPublic)).check(matches(isChecked()));
    }

    //// Test created with guidance from ChatGPT (OpenAI), March 28, 2025
    @Test
    public void testPrivateOptionSelected() {
        onView(withId(R.id.radioPrivate)).perform(click());
        onView(withId(R.id.radioPrivate)).check(matches(isChecked()));
    }
}
