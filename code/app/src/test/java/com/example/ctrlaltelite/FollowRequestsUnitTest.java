package com.example.ctrlaltelite;

import static org.junit.Assert.*;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Unit testing for follow requesting system
 */
public class FollowRequestsUnitTest {

    private List<User> users;
    private List<FollowRequest> followRequests;

    /**
     * Creating test data
     */
    @Before
    public void setUp() {
        // Seed test users with NEW names
        users = new ArrayList<>();
        users.add(new User("Leo Carter", "leocart", null));
        users.add(new User("Emily Zhang", "emzhang", null));
        users.add(new User("Noah Patel", "noahp", null));

        // Seed test follow requests
        followRequests = new ArrayList<>();
        followRequests.add(new FollowRequest("leocart", "emzhang", "Leo Carter", "Emily Zhang", "Pending"));
        followRequests.add(new FollowRequest("noahp", "emzhang", "Noah Patel", "Emily Zhang", "Accepted"));
    }

    /**
     * Confirming correctness getters of follow request class
     */
    @Test
    public void testFollowRequestIsPending() {
        FollowRequest request = followRequests.get(0);
        assertEquals("Pending", request.getStatus());
        assertEquals("leocart", request.getRequesterUserName());
        assertEquals("emzhang", request.getRequestedUserName());
    }

    /**
     * Confirming correctness of setters in follow request class
     */
    @Test
    public void testFollowRequestGrantPermission() {
        FollowRequest request = followRequests.get(0);
        request.setStatus("Accepted");
        assertEquals("Accepted", request.getStatus());
    }

    /**
     * Checking if pending requests are correctly obtained
     */
    @Test
    public void testViewPendingRequests() {
        // Seed mock follow request data
        List<FollowRequest> allRequests = Arrays.asList(
                new FollowRequest("noahp", "leocart", "Noah Patel", "Leo Carter", "Pending"),
                new FollowRequest("emilyw", "leocart", "Emily Wu", "Leo Carter", "Accepted"),
                new FollowRequest("saml", "leocart", "Sam Lee", "Leo Carter", "Pending"),
                new FollowRequest("alexd", "leocart", "Alex Doe", "Leo Carter", "Rejected")
        );

        // Get only pending requests
        List<FollowRequest> pendingRequests = new ArrayList<>();
        for (FollowRequest req : allRequests) {
            if (req.getStatus().equals("Pending")) {
                pendingRequests.add(req);
            }
        }

        // Check count and content
        assertEquals(2, pendingRequests.size());
        assertEquals("noahp", pendingRequests.get(0).getRequesterUserName());
        assertEquals("saml", pendingRequests.get(1).getRequesterUserName());
    }

    /**
     * Simulating adding follow requests
     */
    @Test
    public void testRequestedUsersForCurrentUser() {
        String currentUsername = "emzhang";
        List<FollowRequest> requestsForMe = new ArrayList<>();
        for (FollowRequest req : followRequests) {
            if (req.getRequestedUserName().equals(currentUsername)) {
                requestsForMe.add(req);
            }
        }
        assertEquals(2, requestsForMe.size());
    }

}
