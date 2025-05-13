package edu.sjsu.android.cs175finalproject.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import edu.sjsu.android.cs175finalproject.DashboardAdapter;
import edu.sjsu.android.cs175finalproject.Event;
import edu.sjsu.android.cs175finalproject.EventDatabase;
import edu.sjsu.android.cs175finalproject.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private RecyclerView recyclerView;
    private EventDatabase db;
    private DashboardAdapter adapter;
    private static final int MAX_DASHBOARD_EVENTS = 5;
    private boolean showingPastEvents = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TabLayout tabLayout = binding.tabLayout;

        recyclerView = binding.recyclerEvents;
        db = new EventDatabase(requireContext());

        ArrayList<Event> events = db.getUpcomingEvents(MAX_DASHBOARD_EVENTS);
        adapter = new DashboardAdapter(events);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingPastEvents = tab.getPosition() == 1; // Position 1 is "Past Events"
                refreshEventList();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Not needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Not needed
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshEventList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void refreshEventList() {
        ArrayList<Event> events;

        if (showingPastEvents) {
            events = db.getPastEvents(MAX_DASHBOARD_EVENTS);
        } else {
            events = db.getUpcomingEvents(MAX_DASHBOARD_EVENTS);
        }

        adapter = new DashboardAdapter(events);
        recyclerView.setAdapter(adapter);
    }
}