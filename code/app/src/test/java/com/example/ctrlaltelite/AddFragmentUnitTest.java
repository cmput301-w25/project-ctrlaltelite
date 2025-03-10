package com.example.ctrlaltelite;
import static org.mockito.Mockito.*;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import android.net.Uri;


import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    private LifecycleRegistry lifecycleRegistry;
    private AddFragment addFragment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        addFragment = new AddFragment();
        addFragment.mAuth = mockAuth;
        addFragment.db = mockFirestore;
        addFragment.storage = mockStorage;

        lifecycleRegistry = new LifecycleRegistry(mockLifecycleOwner);
        when(mockLifecycleOwner.getLifecycle()).thenReturn(lifecycleRegistry);
    }

    @Test
    public void testSaveMoodEvent_NoReasonOrImage_ShowsError() {
        // Mock UI state
        addFragment.editReason.setText(""); // No reason provided
        addFragment.imageRef = null; // No image provided

        addFragment.saveMoodEvent("testUser");

        // Verify error toast message
        verify(mockFirestore, never()).collection(anyString());
    }

    @Test
    public void testSaveMoodEvent_WithReason_Success() {
        // Mock UI state
        addFragment.editReason.setText("Feeling great!"); // Reason provided
        addFragment.imageRef = null; // No image

        addFragment.saveMoodEvent("testUser");

        // Verify Firestore save attempt
        verify(mockFirestore, times(1)).collection("Mood Events");
    }

    @Test
    public void testSaveMoodEvent_WithImage_Success() {
        // Mock UI state
        addFragment.editReason.setText(""); // No reason
        addFragment.imageRef = mock(Uri.class); // Image provided

        addFragment.saveMoodEvent("testUser");

        // Verify Firestore save attempt
        verify(mockFirestore, times(1)).collection("Mood Events");
    }
}

