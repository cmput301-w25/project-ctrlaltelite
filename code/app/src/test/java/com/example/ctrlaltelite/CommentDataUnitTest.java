package com.example.ctrlaltelite;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import org.junit.Test;

/**
 * Unit Testing for commenting on mood evenets
 */
public class CommentDataUnitTest {

    /**
     * Checking to see if comment was created correctly
     */
    @Test
    public void testCommentDataCreation() {
        String commentText = "Nice mood!";
        String displayName = "Test User";
        String username = "Test User";
        Timestamp timestamp = Timestamp.now();

        CommentData comment = new CommentData(commentText, displayName, username, timestamp);

        assertEquals(commentText, comment.getText());
        assertEquals(displayName, comment.getUsername());
        assertEquals(timestamp, comment.getTimestamp());
    }

    /**
     * Checking to confirm that a comment is not null
     */
    @Test
    public void testCommentTextNotNull() {
        CommentData comment = new CommentData("Hello", "TestUser", "TestUser", Timestamp.now());
        assertNotNull(comment.getText());
    }
}
