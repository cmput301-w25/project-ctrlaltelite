package com.example.ctrlaltelite;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
/**
 * Unit tests for the sign-up input validation logic.
 */
public class SignUpUnitTest {

    /**
     * Tests that valid sign-up data passes validation.
     */
    @Test
    public void testValidSignUpData() {
        boolean result = SignUp.isSignUpDataValid("user", "user@example.com", "1234567890", "password");
        assertTrue("Expected valid sign-up data to pass", result);
    }

    /**
     * Tests that an empty username causes validation to fail.
     */
    @Test
    public void testEmptyUsername() {
        boolean result = SignUp.isSignUpDataValid("", "user@example.com", "1234567890", "password");
        assertFalse("Expected empty username to fail", result);
    }

    /**
     * Tests that an empty email causes validation to fail.
     */
    @Test
    public void testEmptyEmail() {
        boolean result = SignUp.isSignUpDataValid("user", "", "1234567890", "password");
        assertFalse("Expected empty email to fail", result);
    }

    /**
     * Tests that an empty mobile number causes validation to fail.
     */
    @Test
    public void testEmptyMobile() {
        boolean result = SignUp.isSignUpDataValid("user", "user@example.com", "", "password");
        assertFalse("Expected empty mobile to fail", result);
    }

    /**
     * Tests that an empty password causes validation to fail.
     */
    @Test
    public void testEmptyPassword() {
        boolean result = SignUp.isSignUpDataValid("user", "user@example.com", "1234567890", "");
        assertFalse("Expected empty password to fail", result);
    }

    /**
     * Tests that an invalid email (missing '@') causes validation to fail.
     */
    @Test
    public void testInvalidEmail() {
        // Email missing "@" should fail.
        boolean result = SignUp.isSignUpDataValid("user", "userexample.com", "1234567890", "password");
        assertFalse("Expected invalid email to fail", result);
    }
}
