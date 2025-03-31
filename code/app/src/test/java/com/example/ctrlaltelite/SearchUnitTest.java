package com.example.ctrlaltelite;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
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
 * Unit tests for user search in SearchFragment (US 03.02.01).
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SearchUnitTest {

    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private Task<QuerySnapshot> mockQueryTask;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private QueryDocumentSnapshot mockDocumentSnapshot;
    @Mock
    private UserAdapter mockUserAdapter;

    private SearchFragment fragment;
    private List<User> userList;

    /**
     * Setting up mocked database
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        fragment = new SearchFragment("testCurrentUser");

        userList = new ArrayList<>();
        setPrivateField("db", mockFirestore);
        setPrivateField("userList", userList);
        setPrivateField("userAdapter", mockUserAdapter); // Set the mock UserAdapter

        // Mock Firestore query setup
        when(mockFirestore.collection("users")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.get()).thenReturn(mockQueryTask);
        when(mockQueryTask.isSuccessful()).thenReturn(true);
        when(mockQueryTask.getResult()).thenReturn(mockQuerySnapshot);
        when(mockQueryTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            invocation.getArgument(0, com.google.android.gms.tasks.OnCompleteListener.class).onComplete(mockQueryTask);
            return mockQueryTask;
        });
    }

    /**
     * Setting a field value from the SearchFragment
     * @param fieldName - field who's value we want to set
     * @param value - the value itself
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void setPrivateField(String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = SearchFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(fragment, value);
        field.setAccessible(false);
    }

    /**
     * Getting the field value of SearchFragment
     * @param fieldName - the field whose value we want to obtain
     * @return the obtained value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private Object getPrivateField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = SearchFragment.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(fragment);
        field.setAccessible(false);
        return value;
    }

    /**
     * Tests fetching and filtering users by search query (US 03.02.01).
     */
    @Test
    public void testFetchUsersByQuery() throws NoSuchFieldException, IllegalAccessException {
        // Seed mock data
        when(mockDocumentSnapshot.getString("displayName")).thenReturn("User One", "Other User", "Test Current User");
        when(mockDocumentSnapshot.getString("username")).thenReturn("user1", "otherUser", "testCurrentUser");
        when(mockDocumentSnapshot.getString("email")).thenReturn("user1@test.com", "other@test.com", "test@current.com");
        when(mockDocumentSnapshot.getString("mobile")).thenReturn("123", "456", "789");
        when(mockQuerySnapshot.iterator()).thenReturn(Collections.list(Collections.enumeration(
                List.of(mockDocumentSnapshot, mockDocumentSnapshot, mockDocumentSnapshot))).iterator());

        // Fetch users with query "other"
        fragment.fetchUsers("other");

        List<User> updatedUsers = (List<User>) getPrivateField("userList");
        assertEquals(1, updatedUsers.size());
        assertEquals("otherUser", updatedUsers.get(0).getUsername());

        // Verify currentUser is set
        User currentUser = (User) getPrivateField("currentUser");
        assertEquals("testCurrentUser", currentUser.getUsername());
    }

    /**
     * Tests fetching all users when query is empty.
     */
    @Test
    public void testFetchAllUsersWithEmptyQuery() throws NoSuchFieldException, IllegalAccessException {
        // Seed mock data
        when(mockDocumentSnapshot.getString("displayName")).thenReturn("User One", "Other User", "Test Current User");
        when(mockDocumentSnapshot.getString("username")).thenReturn("user1", "otherUser", "testCurrentUser");
        when(mockDocumentSnapshot.getString("email")).thenReturn("user1@test.com", "other@test.com", "test@current.com");
        when(mockDocumentSnapshot.getString("mobile")).thenReturn("123", "456", "789");
        when(mockQuerySnapshot.iterator()).thenReturn(Collections.list(Collections.enumeration(
                List.of(mockDocumentSnapshot, mockDocumentSnapshot, mockDocumentSnapshot))).iterator());

        // Fetch all users with empty query
        fragment.fetchUsers("");

        List<User> updatedUsers = (List<User>) getPrivateField("userList");
        assertEquals(3, updatedUsers.size());
        assertEquals("user1", updatedUsers.get(0).getUsername());
        assertEquals("otherUser", updatedUsers.get(1).getUsername());
        assertEquals("testCurrentUser", updatedUsers.get(2).getUsername());
    }
}