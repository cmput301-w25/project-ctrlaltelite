package com.example.ctrlaltelite;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ctrlaltelite.AddFragment;
import com.example.ctrlaltelite.MainActivity;
import com.example.ctrlaltelite.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//// Test created with guidance from ChatGPT (OpenAI), March 28, 2025
@RunWith(AndroidJUnit4.class)
public class AttachLocationUITest {

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
                    .commitNow(); // Ensure the fragment is fully loaded
        });
    }

    //// Test created with guidance from ChatGPT (OpenAI), March 28, 2025
    @Test
    public void testEnableLocationToggle() {
        // Click the toggle
        onView(withId(R.id.switch_location)).perform(click());

        // Verify it is checked
        onView(withId(R.id.switch_location)).check(matches(isChecked()));
    }
}
