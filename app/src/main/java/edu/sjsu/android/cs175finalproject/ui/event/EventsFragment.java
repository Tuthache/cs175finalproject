package edu.sjsu.android.cs175finalproject.ui.event;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.sjsu.android.cs175finalproject.Event;
import edu.sjsu.android.cs175finalproject.EventAdapter;
import edu.sjsu.android.cs175finalproject.EventDatabase;
import edu.sjsu.android.cs175finalproject.R;
import edu.sjsu.android.cs175finalproject.databinding.FragmentEventsBinding;

public class EventsFragment extends Fragment {

    private FragmentEventsBinding binding;
    private RecyclerView recyclerView;
    private EventDatabase db;
    private EventAdapter adapter;
    private ToggleButton toggleButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerEvents;
        toggleButton = binding.toggleViewMode;

        db = new EventDatabase(requireContext());
        ArrayList<Event> eventList = db.getUpcomingEvents(Integer.MAX_VALUE);
        adapter = new EventAdapter(eventList);

        // Default: List view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });

        FloatingActionButton fab = binding.fabAddEvent;
        fab.setOnClickListener(v -> showAddEventDialog());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showAddEventDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);

        EditText titleInput = dialogView.findViewById(R.id.input_title);
        EditText descriptionInput = dialogView.findViewById(R.id.input_description);
        Button dateButton = dialogView.findViewById(R.id.button_pick_date);
        TextView dateText = dialogView.findViewById(R.id.selected_date);
        EditText categoryInput = dialogView.findViewById(R.id.input_category);
        EditText reminderInput = dialogView.findViewById(R.id.input_reminder);
        EditText recurrenceInput = dialogView.findViewById(R.id.input_recurrence);
        CheckBox importantCheckbox = dialogView.findViewById(R.id.checkbox_important);

        final long[] selectedDateMillis = {0};

        dateButton.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(getContext(),
                    (datePicker, year, month, day) -> {
                        Calendar selected = Calendar.getInstance();
                        selected.set(year, month, day);
                        selectedDateMillis[0] = selected.getTimeInMillis();
                        dateText.setText(new Date(selectedDateMillis[0]).toString());
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Add Event")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = titleInput.getText().toString();
                    String desc = descriptionInput.getText().toString();
                    String category = categoryInput.getText().toString();
                    int reminder = Integer.parseInt(reminderInput.getText().toString());
                    String recurrence = recurrenceInput.getText().toString();
                    boolean important = importantCheckbox.isChecked();

                    Event event = new Event(
                            0, title, desc, selectedDateMillis[0],
                            category, reminder, recurrence, important
                    );
                    db.insertEvent(event);
                    refreshEventList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshEventList() {
        ArrayList<Event> events = db.getUpcomingEvents(Integer.MAX_VALUE);
        adapter = new EventAdapter(events);
        recyclerView.setAdapter(adapter);

        // Re-apply the layout manager based on current toggle state
        if (toggleButton.isChecked()) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }


}
