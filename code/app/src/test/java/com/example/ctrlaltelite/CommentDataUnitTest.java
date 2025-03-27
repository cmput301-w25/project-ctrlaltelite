package com.example.ctrlaltelite;

import static org.junit.Assert.*;
import org.junit.Test;

public class CommentDataUnitTest {

    @Test
    public void testCommentDataCreation() {
        String commentText = "Nice mood!";
        String displayName = "Test User";
        long timestamp = System.currentTimeMillis();

        CommentData comment = new CommentData(commentText, displayName, timestamp);

        assertEquals(commentText, comment.getText());
        assertEquals(displayName, comment.getUsername());
        assertEquals(timestamp, comment.getTimestamp());
    }

    @Test
    public void testCommentTextNotNull() {
        CommentData comment = new CommentData("Hello", "TestUser", System.currentTimeMillis());
        assertNotNull(comment.getText());
    }
}
