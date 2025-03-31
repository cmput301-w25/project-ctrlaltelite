package com.example.ctrlaltelite;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ctrlaltelite.R;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Objects;

/**
 * Code for the functionality of the map fragment
 */
public class MapFragment extends Fragment {

    private String Username;

    View view;
    TabLayout tabLayout;
    ViewPager2 viewPager2;


    /**
     * Inflating the UI for the fragment map
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_map, container, false);
        ImageButton buttonDrawerToggle = view.findViewById(R.id.buttonDrawerToggle);

        // Retrieve username from Bundle
        Bundle args = getArguments();
        if (args != null) {
            Username = args.getString("username");
        }
        if (Username == null) {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return view;
        }

        tabLayout = view.findViewById(R.id.tabs);
        viewPager2 = view.findViewById(R.id.view_pager);
        ViewPageAdapterMap adapter = new ViewPageAdapterMap(requireActivity(), Username);
        viewPager2.setAdapter(adapter);

        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();

        if (buttonDrawerToggle != null && mainActivity != null) {
            buttonDrawerToggle.setOnClickListener(v -> {
                mainActivity.openDrawer();
            });
        }

        viewPager2.setUserInputEnabled(false);  // Disables swipe gestures

        //Pressing on the notification button
        LottieAnimationView notifButton = view.findViewById(R.id.notif); // Ensure it's LottieAnimationView
        notifButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Functionality for when the notification bell is pressed
             * @param view
             */
            @Override
            public void onClick(View view) {
                if (Username != null) { // Ensure Username is retrieved before navigating
                    ViewFollowRequestsFragment followRequestsFragment = new ViewFollowRequestsFragment(Username);

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).fragmentRepl(followRequestsFragment);
                    }
                } else {
                    Toast.makeText(getContext(), "Error: Username not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         *
         */
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            /**
             * Functionality for when a tab is selected
             * @param tab The tab that was selected
             */
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            /**
             * Do nothing if a tab is unselected
             * @param tab The tab that was unselected
             */
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            /**
             * Do nothing if a tab is reselected
             * @param tab The tab that was reselected.
             */
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            /**
             * Functionality for when a page is selected
             * @param position Position index of the new selected page.
             */
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });
        return view;

    }
}
