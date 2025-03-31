package com.example.ctrlaltelite;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.os.Build;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
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
import java.util.Date;
import java.util.List;

/**
 * Unit test for MapNearbyFragment that updates the 'latestMood' map.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.P}, manifest = Config.NONE)
public class MapNearbyFragmentUnitTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockMoodEventsCollection;
    @Mock
    private Query mockQuery;
    @Mock
    private Task<QuerySnapshot> mockMoodEventsTask;
    @Mock
    private QuerySnapshot mockMoodEventsQuerySnapshot;

    private MapNearbyFragment fragment;

    /**
     * Setting up mocked database
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        // Initialize the Mockito @Mock fields
        MockitoAnnotations.openMocks(this);
        fragment = new MapNearbyFragment();
        setPrivateField("db", mockFirestore);
    }

    /**
     * Helper to set a private field in the fragment via reflection.
     */
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = MapNearbyFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
        field.setAccessible(false);
    }

    /**
     * Scenario 1: If there's no followed user list, or it's empty,
     * 'latestMood' remains empty.
     */
    @Test
    public void testNoFollowedUsers_ZeroInMarkerMap() {
        // Stub out "Mood Events" so it won't be null
        when(mockFirestore.collection("Mood Events")).thenReturn(mockMoodEventsCollection);
        when(mockMoodEventsCollection.whereIn(eq("username"), anyList())).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo(eq("public"), eq(true))).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockMoodEventsTask);

        when(mockMoodEventsTask.addOnCompleteListener(any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);
                    // Mark the task successful but with an empty result
                    when(mockMoodEventsTask.isSuccessful()).thenReturn(true);
                    when(mockMoodEventsTask.getResult()).thenReturn(mockMoodEventsQuerySnapshot);
                    when(mockMoodEventsQuerySnapshot.iterator())
                            .thenReturn(Collections.<QueryDocumentSnapshot>emptyList().iterator());
                    listener.onComplete(mockMoodEventsTask);
                    return mockMoodEventsTask;
                });

        // Call showMoodEventMap with an empty followed list => no data
        List<String> emptyFollowed = Collections.emptyList();
        fragment.showMoodEventMap(emptyFollowed, new GeoPoint(53.50, -113.50));

        // Check that 'latestMood' is empty
        assertTrue("latestMood map should remain empty if followed list is empty.",
                fragment.getMarkerMap().isEmpty());
    }

    /**
     * Scenario 2: If we follow 2 users, each with 1 mood event =>
     * 'latestMood' ends up with 2 entries (one per user).
     */
    @Test
    public void testFollowedTwoUsers_TwoInMarkerMap() {
        // Provide a followed list with 2 users: userA, userB
        List<String> followed = new ArrayList<>();
        followed.add("userA");
        followed.add("userB");

        // Stub out "Mood Events" calls so they're not null
        when(mockFirestore.collection("Mood Events")).thenReturn(mockMoodEventsCollection);
        when(mockMoodEventsCollection.whereIn(eq("username"), eq(followed))).thenReturn(mockQuery);
        when(mockQuery.whereEqualTo("public", true)).thenReturn(mockQuery);
        when(mockQuery.get()).thenReturn(mockMoodEventsTask);

        // Make the .addOnCompleteListener() call the listener immediately
        when(mockMoodEventsTask.addOnCompleteListener(any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    OnCompleteListener<QuerySnapshot> listener = invocation.getArgument(0);

                    // Mark the task as successful
                    when(mockMoodEventsTask.isSuccessful()).thenReturn(true);
                    // Return mock query snapshot
                    when(mockMoodEventsTask.getResult()).thenReturn(mockMoodEventsQuerySnapshot);

                    // Prepare 2 docs to simulate userA / userB mood events
                    MoodEvent eventA = new MoodEvent("😊 Happy","ReasonA","Alone",
                            new Timestamp(new Date()), new GeoPoint(53.51, -113.45),
                            null,"userA",true);
                    eventA.setDocumentId("docA");

                    MoodEvent eventB = new MoodEvent("😢 Sad","ReasonB","WithOthers",
                            new Timestamp(new Date()), new GeoPoint(53.52, -113.46),
                            null,"userB",true);
                    eventB.setDocumentId("docB");

                    QueryDocumentSnapshot docSnapA = mock(QueryDocumentSnapshot.class);
                    QueryDocumentSnapshot docSnapB = mock(QueryDocumentSnapshot.class);
                    when(docSnapA.toObject(MoodEvent.class)).thenReturn(eventA);
                    when(docSnapB.toObject(MoodEvent.class)).thenReturn(eventB);

                    List<QueryDocumentSnapshot> docs = new ArrayList<>();
                    docs.add(docSnapA);
                    docs.add(docSnapB);

                    // Return them in iterator()
                    when(mockMoodEventsQuerySnapshot.iterator()).thenReturn(docs.iterator());

                    // Fire the callback
                    listener.onComplete(mockMoodEventsTask);
                    return mockMoodEventsTask;
                });

        // Call showMoodEventMap with the 2-user list
        fragment.showMoodEventMap(followed, new GeoPoint(53.50, -113.50));

        // latestMood should have 2 items: userA and userB
        assertEquals("Should have 2 distinct user events in map",
                2, fragment.getMarkerMap().size());
        assertTrue(fragment.getMarkerMap().containsKey("userA"));
        assertTrue(fragment.getMarkerMap().containsKey("userB"));
    }
}
