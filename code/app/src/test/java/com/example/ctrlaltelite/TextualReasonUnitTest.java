package com.example.ctrlaltelite;

import org.junit.Test;

public class TextualReasonUnitTest {

    @Test
    public testEmptyTextualReason() {
        private String mockTextualReason = "";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected non-empty textual reason", result);
    }

    @Test
    public testTextualReasonWithTooManyWords() {
        private String mockTextualReason = "I am very lonely";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected Textual Reason with 3 Words or Less", result);
    }

    @Test
    public testTextualReasonWithTooManyCharacter() {
        private String mockTextualReason = "Absolutely insurmountable loneliness";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected Textual Reason with 20 characters or Less");
    }

    @Test
    public testValidTextualReason() {
        private String mockTextualReason = "I am lonely";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertTrue("Valid Textual Reason", result);
    }

}
