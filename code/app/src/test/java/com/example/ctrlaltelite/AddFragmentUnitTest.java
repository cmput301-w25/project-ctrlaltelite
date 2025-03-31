package com.example.ctrlaltelite;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.net.Uri;
import android.text.Editable;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.example.ctrlaltelite.AddFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Unit tests for the AddFragment class.
 * Tests validation logic and Firebase interaction for saving mood events.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE) // Not show warnings
public class AddFragmentUnitTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private FirebaseAuth mockAuth;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private FirebaseStorage mockStorage;
    @Mock
    private LifecycleOwner mockLifecycleOwner;
    @Mock
    private Spinner mockDropdownMood;
    @Mock
    private Spinner mockEditSocialSituation;
    @Mock
    private EditText mockEditTrigger;
    @Mock
    private EditText mockEditReason;
    @Mock
    private Switch mockSwitchLocation;
    @Mock
    private Editable mockEditableReason;

    private LifecycleRegistry lifecycleRegistry;
    private AddFragment addFragment;

    /**
     * Sets up mocks and initializes the fragment before each test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addFragment = new AddFragment();
        addFragment.mAuth = mockAuth;
        addFragment.db = mockFirestore;
        addFragment.storage = mockStorage;

        // Manually set up UI components
        addFragment.dropdownMood = mockDropdownMood;
        addFragment.editSocialSituation = mockEditSocialSituation;
        addFragment.editReason = mockEditReason;
        addFragment.switchLocation = mockSwitchLocation;

        lifecycleRegistry = new LifecycleRegistry(mockLifecycleOwner);
        when(mockLifecycleOwner.getLifecycle()).thenReturn(lifecycleRegistry);

        // Mock EditText behavior
        when(mockEditReason.getText()).thenReturn(mockEditableReason);

        // Mock Switch behavior
        when(mockSwitchLocation.isChecked()).thenReturn(false);

    }

    /**
     * Tests that a mood event is not saved when no reason or image is provided.
     */
    @Test
    public void testSaveMoodEvent_NoReasonOrImage_ShowsError() {
        // Mock UI state
        when(mockEditableReason.toString()).thenReturn(""); // No reason provided
        addFragment.imageRef = null; // No image provided

        // Mock mood and social situation selection
        when(mockDropdownMood.getSelectedItemPosition()).thenReturn(1);
        when(mockDropdownMood.getSelectedItem()).thenReturn("Happy");
        when(mockEditSocialSituation.getSelectedItemPosition()).thenReturn(1);
        when(mockEditSocialSituation.getSelectedItem()).thenReturn("Alone");

        addFragment.saveMoodEvent("testUser");

        // Verify error toast message
        verify(mockFirestore, never()).collection(anyString());
    }
}
