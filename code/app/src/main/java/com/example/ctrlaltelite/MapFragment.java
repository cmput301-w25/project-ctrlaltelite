package com.example.ctrlaltelite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;


/**
 * The {@code MapFragment} displays a map interface where users can
 * view mood-related location data.
 */

public class MapFragment extends Fragment {
    /**
     * Default constructor required for fragment creation.
     */
    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment is first created.
     *
     * @param savedInstanceState The saved instance state, if available.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Creates and returns the view hierarchy for this fragment.
     *
     * @param inflater  The LayoutInflater to inflate the layout.
     * @param container The parent view that the fragment UI will attach to.
     * @param savedInstanceState The saved instance state, if available.
     * @return The root view of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
}