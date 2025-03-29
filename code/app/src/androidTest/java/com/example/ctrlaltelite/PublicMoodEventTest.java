package com.example.ctrlaltelite;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.ctrlaltelite.MainActivity;
import com.example.ctrlaltelite.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

public class PublicMoodEventTest {

    @Test
    public void testSelectingPublicRadioButton_marksPostAsPublic() {
        onView(withId(R.id.radioPublic)).perform(click());
        onView(withId(R.id.radioPublic)).check(matches(isChecked()));
    }

    @Test
    public void testSelectingPrivateRadioButton_marksPostAsPrivate() {
        onView(withId(R.id.radioPrivate)).perform(click());
        onView(withId(R.id.radioPrivate)).check(matches(isChecked()));
    }

}
