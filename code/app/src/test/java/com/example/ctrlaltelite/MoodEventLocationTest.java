package com.example.ctrlaltelite;

import static org.junit.Assert.*;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;
import org.junit.Test;

// Test created with guidance from ChatGPT (OpenAI), March 28, 2025
public class MoodEventLocationTest {

    // Test created with guidance from ChatGPT (OpenAI), March 28, 2025

    @Test
    public void testMoodEvent_withLocation_shouldStoreGeoPoint() {
        GeoPoint location = new GeoPoint(53.5461, -113.4938); // Example: Edmonton
        MoodEvent moodEvent = new MoodEvent("😊 Happy", "Sunny walk", "Alone",
                Timestamp.now(), location, null, "user123", true);

        assertNotNull(moodEvent.getLocation());
        assertEquals(location, moodEvent.getLocation());
    }

    // Test created with guidance from ChatGPT (OpenAI), March 28, 2025
    @Test
    public void testMoodEvent_withoutLocation_shouldHaveNullGeoPoint() {
        MoodEvent moodEvent = new MoodEvent("😢 Sad", "Rainy day", "Alone",
                Timestamp.now(), null, null, "user123", false);

        assertNull(moodEvent.getLocation());
    }
}
