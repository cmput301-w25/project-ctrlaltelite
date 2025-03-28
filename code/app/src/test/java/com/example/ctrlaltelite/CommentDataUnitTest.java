package com.example.ctrlaltelite;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import org.junit.Test;

public class CommentDataUnitTest {

    @Test
    public void testCommentDataCreation() {
        String commentText = "Nice mood!";
        String displayName = "Test User";
        Timestamp timestamp = Timestamp.now();

        CommentData comment = new CommentData(commentText, displayName, timestamp);

        assertEquals(commentText, comment.getText());
        assertEquals(displayName, comment.getUsername());
        assertEquals(timestamp, comment.getTimestamp());
    }

    @Test
    public void testCommentTextNotNull() {
        CommentData comment = new CommentData("Hello", "TestUser", Timestamp.now());
        assertNotNull(comment.getText());
    }
}
