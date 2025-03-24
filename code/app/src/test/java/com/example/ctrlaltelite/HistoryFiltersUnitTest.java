package com.example.ctrlaltelite;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

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
        setPrivateField("Username", "testUser"); // Corrected to match HomeFragment

        // Seed test data into allMoodEvents (source list)
        MoodEvent recentHappy = new MoodEvent("😊 Happy", "Good day with stress", "Alone", Timestamp.now(), null, null, "testUser", true);
        MoodEvent recentSad = new MoodEvent("😢 Sad", "Bad day", "With friends", Timestamp.now(), null, null, "testUser", true);
        MoodEvent oldSad = new MoodEvent("😢 Sad", "Old event", "Alone", new Timestamp(getDaysAgo(10)), null, null, "testUser", true);
        allMoodEvents.add(recentHappy);
        allMoodEvents.add(recentSad);
        allMoodEvents.add(oldSad);
    }

    private void setPrivateField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = HomeFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
        field.setAccessible(false);
    }

    private Object getPrivateField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = HomeFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(fragment);
        field.setAccessible(false);
        return value;
    }

    private Date getDaysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return cal.getTime();
    }

    /**
     * Tests filtering mood events for the last 7 days (US 04.02.01).
     */
    @Test
    public void testFilterByLast7Days() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField("weekFilter", true); // Matches HomeFragment’s field
        fragment.applyFilters(); // Correct method name

        List<MoodEvent> updatedEvents = (List<MoodEvent>) getPrivateField("moodEvents");
        assertEquals(2, updatedEvents.size()); // Only recent events
        assertEquals("😊 Happy", updatedEvents.get(0).getEmotionalState());
        assertEquals("😢 Sad", updatedEvents.get(1).getEmotionalState());
    }

    /**
     * Tests filtering mood events by emotional state (US 04.03.01).
     */
    @Test
    public void testFilterByEmotionalState() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField("moodFilter", "😊 Happy"); // Matches HomeFragment’s field
        fragment.applyFilters(); // Correct method name

        List<MoodEvent> updatedEvents = (List<MoodEvent>) getPrivateField("moodEvents");
        assertEquals(1, updatedEvents.size());
        assertEquals("😊 Happy", updatedEvents.get(0).getEmotionalState());
    }

    /**
     * Tests filtering mood events by reason text (US 04.04.01).
     */
    @Test
    public void testFilterByReason() throws NoSuchFieldException, IllegalAccessException {
        setPrivateField("reasonFilter", "stress"); // Matches HomeFragment’s field
        fragment.applyFilters(); // Correct method name

        List<MoodEvent> updatedEvents = (List<MoodEvent>) getPrivateField("moodEvents");
        assertEquals(1, updatedEvents.size());
        assertEquals("Good day with stress", updatedEvents.get(0).getReason());
    }
}