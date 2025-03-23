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
 * AttendeeMyEvent class displays the "My Events" screen for attendees, where they can navigate
 * through various event-related tabs using a ViewPager2 and a TabLayout. It includes navigation
 * to home, scan, and profile screens through a BottomNavigationView.
 */
public class FollowingNavButton extends Fragment {

    private String Username;







    View view;
    TabLayout tabLayout;
    ViewPager2 viewPager2;

    /**
     * Initializes the activity, sets up the BottomNavigationView navigation, and configures
     * the TabLayout and ViewPager2 for event category navigation.
     *
     * @param savedInstanceState If the activity is being re-initialized, this contains the most recent data.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_following_nav_button, container, false);
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
        ViewPageAdapter adapter = new ViewPageAdapter(requireActivity(), Username);
        viewPager2.setAdapter(adapter);

        // Get a reference to the MainActivity so we can call openDrawer()
        MainActivity mainActivity = (MainActivity) getActivity();

        if (buttonDrawerToggle != null && mainActivity != null) {
            buttonDrawerToggle.setOnClickListener(v -> {
                mainActivity.openDrawer();
            });
        }

        //Pressing on the notification button
        LottieAnimationView notifButton = view.findViewById(R.id.notif); // Ensure it's LottieAnimationView
        notifButton.setOnClickListener(new View.OnClickListener() {
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







        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
                Objects.requireNonNull(tabLayout.getTabAt(position)).select();
            }
        });
        return view;

    }
}
