package com.example.ctrlaltelite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Test;

//// Test created with guidance from ChatGPT (OpenAI), March 28, 2025

/**
 * Testing public and public mood evenets
 */
public class PublicMoodEventUnitTest {

    /**
     * Testing if a mood public is public
     */
    @Test
    public void testPublicMoodEvent() {
        MoodEvent moodEvent = new MoodEvent("😊 Happy", "Great day!", "With friends",
                Timestamp.now(), new GeoPoint(53.5, -113.5), null, "user123", true);

        assertTrue(moodEvent.isPublic());
    }

    /**
     * Testing if a mood event is private
     */
    @Test
    public void testPrivateMoodEvent() {
        MoodEvent moodEvent = new MoodEvent("😢 Sad", "Tough day", "Alone",
                Timestamp.now(), null, null, "user123", false);

        assertFalse(moodEvent.isPublic());
    }
}

