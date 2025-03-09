package com.example.ctrlaltelite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
/**
 * Unit tests for the Login input validation logic.
 */
public class LoginUnitTest {
    /**
     * Tests that valid username and password inputs pass validation.
     */
    @Test
    public void testValidLoginInputs() {
        // Both username and password are non-empty.
        boolean result = Login.isInputValid("user", "password");
        assertTrue("Expected valid inputs to pass", result);
    }

    /**
     * Tests that an empty username causes validation to fail.
     */
    @Test
    public void testEmptyUsername() {
        boolean result = Login.isInputValid("", "password");
        assertFalse("Expected empty username to fail", result);
    }

    /**
     * Tests that an empty password causes validation to fail.
     */
    @Test
    public void testEmptyPassword() {
        boolean result = Login.isInputValid("user", "");
        assertFalse("Expected empty password to fail", result);
    }

    /**
     * Tests that both username and password being empty causes validation to fail.
     */
    @Test
    public void testBothEmpty() {
        boolean result = Login.isInputValid("", "");
        assertFalse("Expected both empty to fail", result);
    }
}
