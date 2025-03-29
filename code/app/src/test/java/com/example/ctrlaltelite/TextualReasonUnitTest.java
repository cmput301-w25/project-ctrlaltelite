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
     * Checking to see if AddFragment invalidates a textual reason with too many characters (20 or more)
     */
    @Test
    public void testTextualReasonWithTooManyCharacter() {
        String mockTextualReason = "Ridiculously Insurmountable Loneliness. I have spent dozens and dozens" +
                "of hours on this crazy long project to the point where I am the total epitome of sheer loneliness. My daily routine consists of" +
                "constant errors when I am unit testing that I want to pull my hair out. (I am pretty sure we are at 200 characters now but just in case hopefully now" +
                "the world limit has been reached";
        boolean result = AddFragment.isTextualReasonValid(mockTextualReason);
        assertFalse("Expected Textual Reason with 200 characters or Less", result);
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
