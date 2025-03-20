package com.example.ctrlaltelite;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for mood event viewing and editing functionalities: US 01.04.01 and 01.05.01
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE) // Not show warnings
public class MoodEventViewEditTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private Query mockQuery;
    @Mock
    private DocumentReference mockDocRef;
    @Mock
    private Task<Void> mockTask;
    @Mock
    private MoodEventAdapter mockAdapter;

    @Mock
    private Task<QuerySnapshot> mockQueryTask;

    @Mock
    private CollectionReference mockCollectionRef;

    @Mock
    private QueryDocumentSnapshot mockDocumentSnapshot;

    @Mock
    private QuerySnapshot mockQuerySnapshot;

    private HomeFragment fragment;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        fragment = new HomeFragment();

        // Use reflection to set private fields
        setPrivateField("db", mockFirestore);
        setPrivateField("moodEvents", new ArrayList<MoodEvent>());
        setPrivateField("adapter", mockAdapter);
        setPrivateField("Username", "testUser");

        // Ensure Task chaining works by default
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);
        when(mockQueryTask.addOnSuccessListener(any())).thenReturn(mockQueryTask);
        when(mockQueryTask.addOnFailureListener(any())).thenReturn(mockQueryTask);

        // Mock Firestore setup
        when(mockFirestore.collection("Mood Events")).thenReturn(mockCollectionRef);
        when(mockCollectionRef.whereEqualTo("username", "testUser")).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockQueryTask);
        when(mockFirestore.collection("Mood Events").document(any())).thenReturn(mockDocRef);
        when(mockDocRef.set(any())).thenReturn(mockTask);
    }

    private void setPrivateField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = HomeFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
        field.setAccessible(false);
    }

    private Object getPrivateField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = HomeFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(fragment);
        field.setAccessible(false);
        return value;
    }

    /**
     * Tests the successful retrieval and viewing of mood events from Firestore. US 01.04.01
     * <p>
     * Verifies that {@link HomeFragment#fetchMoodEvents} correctly fetches mood events for the
     * test user, updates the {@code moodEvents} list, and notifies the adapter when the operation
     * succeeds.
     */
    @Test
    public void testFetchMoodEvents_Success() throws NoSuchFieldException, IllegalAccessException {
        List<MoodEvent> moodEventsList = (List<MoodEvent>) getPrivateField("moodEvents");
        MoodEvent moodEvent = new MoodEvent("Happy", "Good day", "Alone", Timestamp.now(), null, null, "testUser");
        moodEvent.setDocumentId("testId");

        // Mock the QuerySnapshot to return a single document
        when(mockDocumentSnapshot.toObject(MoodEvent.class)).thenReturn(moodEvent);
        when(mockDocumentSnapshot.getId()).thenReturn("testId");
        when(mockQuerySnapshot.iterator()).thenReturn(Collections.singletonList(mockDocumentSnapshot).iterator());
        when(mockQueryTask.isSuccessful()).thenReturn(true);
        when(mockQueryTask.getResult()).thenReturn(mockQuerySnapshot);

        when(mockQueryTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            com.google.android.gms.tasks.OnCompleteListener<QuerySnapshot> listener =
                    invocation.getArgument(0);
            listener.onComplete(mockQueryTask); // Trigger the fragment's complete callback
            return mockQueryTask;
        });

        fragment.fetchMoodEvents();

        verify(mockCollectionRef).whereEqualTo("username", "testUser");
        verify(mockQuery).get();
        verify(mockAdapter, times(2)).notifyDataSetChanged();
        assert moodEventsList.size() == 1;
        assert moodEventsList.get(0).equals(moodEvent);
        assert moodEventsList.get(0).getDocumentId().equals("testId");
    }

    /**
     * Tests the successful update of a mood event in Firestore. US 01.05.01
     * <p>
     * Verifies that {@link HomeFragment#updateMoodEventInFirestore} correctly updates the Firestore
     * document, notifies the adapter of changes, and maintains the mood event in the list when the
     * operation succeeds.
     */
    @Test
    public void testUpdateMoodEventInFirestore_Success() throws NoSuchFieldException, IllegalAccessException {
        MoodEvent moodEvent = new MoodEvent("Happy", "Good day", "Alone", Timestamp.now(), null, null, "testUser");
        moodEvent.setDocumentId("testId");
        List<MoodEvent> moodEventsList = (List<MoodEvent>) getPrivateField("moodEvents");
        moodEventsList.add(moodEvent);

        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, com.google.android.gms.tasks.OnSuccessListener.class).onSuccess(null);
            return mockTask; // Ensure chaining
        });

        fragment.updateMoodEventInFirestore(moodEvent, 0);

        verify(mockDocRef).set(moodEvent);
        verify(mockAdapter, times(2)).notifyDataSetChanged();
        assert moodEventsList.get(0) == moodEvent;
    }

    /**
     * Tests the failure case of updating a mood event in Firestore. US 01.05.01
     * <p>
     * Verifies that {@link HomeFragment#updateMoodEventInFirestore} attempts to update the Firestore
     * document but does not notify the adapter when the operation fails.
     */
    @Test
    public void testUpdateMoodEventInFirestore_Failure() throws NoSuchFieldException, IllegalAccessException {
        MoodEvent moodEvent = new MoodEvent("Sad", "Bad day", "With others", Timestamp.now(), null, null, "testUser");
        moodEvent.setDocumentId("testId");
        List<MoodEvent> moodEventsList = (List<MoodEvent>) getPrivateField("moodEvents");
        moodEventsList.add(moodEvent);

        // Override default success behavior to do nothing
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, com.google.android.gms.tasks.OnFailureListener.class).onFailure(new Exception("Firestore error"));
            return mockTask;
        });

        fragment.updateMoodEventInFirestore(moodEvent, 0);

        verify(mockDocRef).set(moodEvent);
        verify(mockAdapter, never()).notifyDataSetChanged();
    }

    @Test
    public void testDeleteMoodIsSuccessful() throws NoSuchFieldException, IllegalAccessException {

        // Adding two mood events for our test uesr
        MoodEvent test1MoodEvent = new MoodEvent("Happy", "Good day", "Alone", Timestamp.now(), null, null, "testUser");
        test1MoodEvent.setDocumentId("test1Id");

        List<MoodEvent> moodEventsList = (List<MoodEvent>) getPrivateField("moodEvents");
        moodEventsList.add(test1MoodEvent);

        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, com.google.android.gms.tasks.OnSuccessListener.class).onSuccess(null);
            return mockTask; // Ensure chaining
        });

        // Deleting the first mood event
        fragment.DeleteMoodEventAndUpdateDatabaseUponDeletion(test1MoodEvent);

        // Ensuring a DocumentReference was deleted
        verify(mockDocRef).delete();

    }
}