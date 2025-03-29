package com.example.ctrlaltelite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FollowersCountFragment extends Fragment {
    private FirebaseFirestore db;
    private ListView followersListView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    public FollowersCountFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers_count, container, false);
        followersListView = view.findViewById(R.id.list_followers);
        ImageButton backButton = view.findViewById(R.id.back);
        db = FirebaseFirestore.getInstance();

        userAdapter = new UserAdapter(requireContext(), userList);
        followersListView.setAdapter(userAdapter);



        Bundle args = getArguments();
        String username = args != null ? args.getString("username") : null;

        if (username == null) {
            Toast.makeText(getContext(), "Username missing", Toast.LENGTH_SHORT).show();
            return view;
        }


        backButton.setOnClickListener(v -> {
            ProfileFragment profileFragment = new ProfileFragment();

            // Pass the username back
            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            profileFragment.setArguments(bundle);

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).fragmentRepl(profileFragment);
            }
        });


        db.collection("FollowRequests")
                .whereEqualTo("Requestee's Username", username)
                .whereEqualTo("Status", "Accepted")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    List<String> followers = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String displayName = doc.getString("Requester's Display Name");
                        String user = doc.getString("Requester's Username");
                        if (displayName != null && user != null) {
                            userList.add(new User(displayName, user, null));
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                });

        return view;
    }
}
