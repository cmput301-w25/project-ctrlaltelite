package com.example.ctrlaltelite;

import static org.junit.Assert.assertEquals;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Unit tests for mood history filtering in HomeFragment (US 04.02.01, 04.03.01, 04.04.01).
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class HistoryFiltersUnitTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private MoodEventAdapter mockAdapter;

    private HomeFragment fragment;
    private List<MoodEvent> moodEvents;
    private List<MoodEvent> allMoodEvents;

    /**
     * Setting up the mock database
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        fragment = new HomeFragment();

        moodEvents = new ArrayList<>();
        allMoodEvents = new ArrayList<>();
        setPrivateField("db", mockFirestore);
        setPrivateField("moodEvents", moodEvents);
        setPrivateField("allMoodEvents", allMoodEvents);
        setPrivateField("adapter", mockAdapter);
        setPrivateField("Username", "testUser"); // Ensure this matches HomeFragment's expected field

        // Seed test data into allMoodEvents (source list)
        // Create explicit timestamps to control ordering
        Timestamp now = Timestamp.now();
        // Create a timestamp 1 second earlier for ordering purposes
        Timestamp oneSecondEarlier = new Timestamp(new Date(now.toDate().getTime() - 1000));
        // "recentSad" is the most recent event (should appear first)
        MoodEvent recentSad = new MoodEvent("😢 Sad", "Bad day", "With friends", now, null, null, "testUser", true);
        // "recentHappy" is slightly older (should appear second)
        MoodEvent recentHappy = new MoodEvent("😊 Happy", "Good day with stress", "Alone", oneSecondEarlier, null, null, "testUser", true);
        // An older event beyond 7 days should be filtered out
        MoodEvent oldSad = new MoodEvent("😢 Sad", "Old event", "Alone", new Timestamp(getDaysAgo(10)), null, null, "testUser", true);

        allMoodEvents.add(recentHappy);
        allMoodEvents.add(recentSad);
        allMoodEvents.add(oldSad);
    }

    /**
     * Method for setting fields of the HomeFragment
     * @param fieldName name of field in the HomeFragment
     * @param value - what our field value will be now
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setPrivateField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = HomeFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
        field.setAccessible(false);
    }

    /**
     * Getter for fields of the field fragment
     * @param fieldName - the field whose value we want to obtain
     * @return the obtain parameter
     * @param <T>
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = HomeFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        T value = (T) field.get(fragment);
        field.setAccessible(false);
        return value;
    }

    /**
     * Getting the date from some number of days ago
     * @param days - how far back we want to go
     * @return
     */
    private Date getDaysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return cal.getTime();
    }

    /**
     * Tests filtering mood events for the last 7 days (US 04.02.01).
     * The filtered events must be in reverse chronological order.
     */
    @Test
    public void testFilterByLast7Days() throws NoSuchFieldException, IllegalAccessException {
        // Enable the 7-day filter
        setPrivateField("weekFilter", true);
        fragment.applyFilters();

        List<MoodEvent> updatedEvents = getPrivateField("moodEvents");
        // Expect only the two recent events (old event filtered out)
        assertEquals("Expected 2 events from the last 7 days", 2, updatedEvents.size());
        // Verify reverse chronological order:
        // Most recent ("😢 Sad") should be at index 0,
        // then "😊 Happy" at index 1.
        assertEquals("Most recent event should be '😢 Sad'", "😢 Sad", updatedEvents.get(0).getEmotionalState());
        assertEquals("Second event should be '😊 Happy'", "😊 Happy", updatedEvents.get(1).getEmotionalState());
    }

    /**
     * Tests filtering mood events by emotional state (US 04.03.01).
     */
    @Test
    public void testFilterByEmotionalState() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField("moodFilter", "😊 Happy");
        fragment.applyFilters();

        List<MoodEvent> updatedEvents = getPrivateField("moodEvents");
        assertEquals("Expected 1 event with emotional state '😊 Happy'", 1, updatedEvents.size());
        assertEquals("😊 Happy", updatedEvents.get(0).getEmotionalState());
    }

    /**
     * Tests filtering mood events by reason text (US 04.04.01).
     */
    @Test
    public void testFilterByReason() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField("reasonFilter", "stress");
        fragment.applyFilters();

        List<MoodEvent> updatedEvents = getPrivateField("moodEvents");
        assertEquals("Expected 1 event with 'stress' in reason", 1, updatedEvents.size());
        assertEquals("Good day with stress", updatedEvents.get(0).getReason());
    }
}
