package edu.sjsu.android.cs175finalproject.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup title
        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Setup the RecyclerView for events
        recyclerView = binding.recyclerEvents;
        db = new EventDatabase(requireContext());

        // Get only the top 5 upcoming events
        ArrayList<Event> upcomingEvents = db.getUpcomingEvents(MAX_DASHBOARD_EVENTS);

        adapter = new DashboardAdapter(upcomingEvents);

        // Use LinearLayoutManager for the dashboard
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the events when returning to this fragment
        refreshEventList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void refreshEventList() {
        ArrayList<Event> events = db.getUpcomingEvents(MAX_DASHBOARD_EVENTS);
        adapter = new DashboardAdapter(events);
        recyclerView.setAdapter(adapter);
    }
}