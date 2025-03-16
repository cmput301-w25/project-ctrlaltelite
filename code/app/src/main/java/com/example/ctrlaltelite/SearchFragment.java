package com.example.ctrlaltelite;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import com.airbnb.lottie.LottieAnimationView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment {

    private FirebaseFirestore db;
    private ListView userListView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private EditText searchUserInput;
    private String currentUserUsername;
    private User currentUser;




    public SearchFragment(String currentUserUsername) {
        this.currentUserUsername = currentUserUsername;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchUserInput = view.findViewById(R.id.search_user_input);
        userListView = view.findViewById(R.id.user_list);

        userAdapter = new UserAdapter(requireContext(), userList);
        userListView.setAdapter(userAdapter);

        ImageButton buttonDrawerToggle = view.findViewById(R.id.buttonDrawerToggle);

        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();

        if (buttonDrawerToggle != null && mainActivity != null) {
            buttonDrawerToggle.setOnClickListener(v -> {
                mainActivity.openDrawer();
            });
        }


        // Fetch all users (empty query shows all)
        fetchUsers("");

        // Real-time search as user types
        searchUserInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                fetchUsers(query);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Go to OtherUserProfileFragment on user clicking on the user in search list
        userListView.setOnItemClickListener((parent, view1, position, id) -> {
            User selectedUser = userList.get(position);
            OtherUserProfileFragment userProfileFragment = new OtherUserProfileFragment(currentUser, selectedUser);
            Bundle bundle = new Bundle();
            bundle.putString("displayName", selectedUser.getUsername()); // Im using username as name for now (TO DO: add name in signup)
            bundle.putString("clickedUsername", selectedUser.getUsername()); // Put clicked user's username in the bundle
            userProfileFragment.setArguments(bundle);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).fragmentRepl(userProfileFragment);
            }
        });

        LottieAnimationView notifButton = view.findViewById(R.id.notif); // Ensure it's LottieAnimationView

        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewFollowRequestsFragment followRequestsFragment = new ViewFollowRequestsFragment(currentUserUsername);
                Bundle bundle = new Bundle();

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).fragmentRepl(followRequestsFragment);
                }
            }
        });
        return view;
    }

    private void fetchUsers(String query) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String username = document.getString("username");
                            String email = document.getString("email");
                            String mobile = document.getString("mobile");
                            User user = new User(username, username, null); // Username as name, no profilePhotoUrl yet
                            user.setEmail(email);
                            user.setMobile(mobile);

                            if (username.equals(currentUserUsername)) {
                                currentUser = user;
                            }

                            String usernameLower = username.toLowerCase();
                            if (query.isEmpty() || usernameLower.contains(query)) {
                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                        Log.d("SearchFragment", "Fetched " + userList.size() + " users for query: " + query);
                    } else {
                        Log.w("SearchFragment", "Error fetching users", task.getException());
                        Toast.makeText(getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}