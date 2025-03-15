package com.example.ctrlaltelite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
/**
 * The {@code FollowingFragment} displays the list of users that the logged-in user is following.
 * This fragment will later be updated to fetch and show followed users' mood updates.
 */


public class FollowingFragment extends Fragment {

    /**
     * Default constructor.
     */
    public FollowingFragment() {
        // Required empty public constructor
    }


    /**
     * Called when the fragment is created.
     *
     * @param savedInstanceState A saved instance state if the fragment is re-initialized.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Inflates the UI layout for the "Following" section.
     *
     * @param inflater           The LayoutInflater used to inflate views.
     * @param container          The parent view the fragment's UI should be attached to.
     * @param savedInstanceState A saved instance state if the fragment is re-initialized.
     * @return The View object containing the fragment UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        ImageButton buttonDrawerToggle = view.findViewById(R.id.buttonDrawerToggle);

        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();

        if (buttonDrawerToggle != null && mainActivity != null) {
            buttonDrawerToggle.setOnClickListener(v -> {
                mainActivity.openDrawer();
            });
        }
        return view;
    }




}