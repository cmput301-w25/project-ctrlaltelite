package com.example.ctrlaltelite;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Testing the Conditions for Textual Reason in AddFragment (and by extension HomeFragment too as the same conditions
 *      are checked in each class) - US 02.01
 */
public class TextualReasonUnitTest {

    /**
     * Checking to see if AddFragment invalidates an empty textual reason
     */
    @Test
    public void testEmptyTextualReason() {
        String mockTextualReason = "";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected non-empty textual reason", result);
    }

    /**
     * Checking to see if AddFragment invalidates a textual reason with too many words (4 or more)
     */
    @Test
    public void testTextualReasonWithTooManyWords() {
        String mockTextualReason = "I am very lonely";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected Textual Reason with 3 Words or Less", result);
    }

    /**
     * Checking to see if AddFragment invalidates a textual reason with too many characters (20 or more)
     */
    @Test
    public void testTextualReasonWithTooManyCharacter() {
        String mockTextualReason = "Absolutely insurmountable loneliness";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected Textual Reason with 20 characters or Less", result);
    }

    /**
     * Checking to see if AddFragment validates a valid textual reason
     */
    @Test
    public void testValidTextualReason() {
        String mockTextualReason = "I am lonely";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assert result;
    }

}
