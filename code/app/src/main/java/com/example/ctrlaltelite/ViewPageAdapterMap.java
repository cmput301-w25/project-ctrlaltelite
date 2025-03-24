package com.example.ctrlaltelite;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * This class is an adapter for managing the fragments in a ViewPager2 widget.
 * It provides the appropriate fragment based on the position within the ViewPager.
 * The fragments displayed correspond to different types of events: confirmed, pending, and declined.
 */
public class ViewPageAdapterMap extends FragmentStateAdapter {

    private String username;

    /**
     * Constructor for the ViewPageAdapter.
     *
     * @param fragmentActivity The FragmentActivity to associate with the adapter. This is used to manage fragment transactions.
     */
    public ViewPageAdapterMap(@NonNull FragmentActivity fragmentActivity, String username) {
        super(fragmentActivity);
        this.username = username;
    }

    /**
     * Returns the fragment to be displayed for the given position.
     *
     * @param position The position of the current item in the ViewPager.
     * @return The appropriate Fragment to be displayed.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the corresponding fragment based on the position
        switch (position) {
            case 0:
                MapNearbyFragment mapNearbyFragment = new MapNearbyFragment();
                Bundle bundle1 = new Bundle();
                bundle1.putString("username", username); // Pass the username
                mapNearbyFragment.setArguments(bundle1);
                return mapNearbyFragment; // Show RecentMoodEvents fragment for position 0
            case 1:
                MapFollowingFragment mapFollowingFragment = new MapFollowingFragment();
                Bundle bundle2 = new Bundle();
                bundle2.putString("username", username); // Pass the username
                mapFollowingFragment.setArguments(bundle2);
                return mapFollowingFragment;  // Show AllMoodEvents fragment for position 1

            case 2:
                MapHistoryFragment mapHistoryFragment = new MapHistoryFragment();
                Bundle bundle3 = new Bundle();
                bundle3.putString("username", username); // Pass the username
                mapHistoryFragment.setArguments(bundle3);
                return mapHistoryFragment;

            default:
                return new MapNearbyFragment();  // Default: Show RecentMoodEvents fragment
        }
    }

    /**
     * Returns the total number of pages (fragments) in the ViewPager.
     *
     * @return The number of fragments.
     */
    @Override
    public int getItemCount() {
        return 3;  // There are three fragments: Recent and following
    }
}
