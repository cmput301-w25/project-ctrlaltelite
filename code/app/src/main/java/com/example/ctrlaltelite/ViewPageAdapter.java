package com.example.ctrlaltelite;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * This class is an adapter for managing the fragments in a ViewPager2 widget.
 * It provides the appropriate fragment based on the position within the ViewPager.
 * The fragments displayed correspond to different types of events: confirmed, pending, and declined.
 */
public class ViewPageAdapter extends FragmentStateAdapter {

    /**
     * Constructor for the ViewPageAdapter.
     *
     * @param fragmentActivity The FragmentActivity to associate with the adapter. This is used to manage fragment transactions.
     */
    public ViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
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
                return new FollowingRecent();  // Show RecentMoodEvents fragment for position 0
            case 1:
                return new FollowingFragment();  // Show AllMoodEvents fragment for position 1
            default:
                return new FollowingRecent();  // Default: Show RecentMoodEvents fragment
        }
    }

    /**
     * Returns the total number of pages (fragments) in the ViewPager.
     *
     * @return The number of fragments.
     */
    @Override
    public int getItemCount() {
        return 2;  // There are two fragments: Recent and following
    }
}
