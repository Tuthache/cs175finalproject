package edu.sjsu.android.cs175finalproject.ui.event;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    private Spinner sortFilterSpinner;
    private Spinner categoryFilterSpinner;
    private ArrayAdapter<String> dynamicCategoryAdapter;
    private Set<String> categorySet = new HashSet<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerEvents;
        toggleButton = binding.toggleViewMode;
        sortFilterSpinner = binding.spinnerSortFilter;
        categoryFilterSpinner = binding.spinnerCategoryFilter;

        // init database and load data
        db = new EventDatabase(requireContext());
        ArrayList<Event> eventList = db.getUpcomingEvents(Integer.MAX_VALUE);
        for (Event e : eventList) categorySet.add(e.getCategory());

        // setup adapter and attach swipe support
        adapter = new EventAdapter(getContext(), eventList);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(adapter.getSwipeCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);

        adapter.setOnEventActionListener(new EventAdapter.OnEventActionListener() {
            @Override
            public void onEdit(Event event) {
                db.updateEvent(event);
                refreshEventList();
            }

            @Override
            public void onDelete(Event event) {
                db.deleteEvent(event.getId());
                refreshEventList();
            }
        });

        // list vs grid view, default to list view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            } else {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            }
        });

        // add event button
        FloatingActionButton fab = binding.fabAddEvent;
        fab.setOnClickListener(v -> showAddEventDialog());

        // search
        SearchView searchView = root.findViewById(R.id.search_events);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        // filter
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.sort_filter_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortFilterSpinner.setAdapter(spinnerAdapter);

        sortFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: adapter.resetFilters(); break;
                    case 1: adapter.filterByImportantOnly(); break;
                    case 2: adapter.sortByTimeAscending(); break;
                    case 3: adapter.sortByTimeDescending(); break;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        updateCategorySpinner();

        categoryFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String category = parent.getItemAtPosition(position).toString();
                if (category.equals("All")) {
                    adapter.resetFilters();
                } else {
                    adapter.filterByCategory(category);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        return root;
    }

    private void updateCategorySpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("All");
        categories.addAll(categorySet);
        dynamicCategoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        dynamicCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryFilterSpinner.setAdapter(dynamicCategoryAdapter);
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
        Spinner recurrenceSpinner = dialogView.findViewById(R.id.spinner_recurrence);
        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.recurrence_options,
                android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceSpinner.setAdapter(recurrenceAdapter);
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

        Spinner recurrenceDurationSpinner = dialogView.findViewById(R.id.spinner_recurrence_duration);

        // fill dropdown
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.recurrence_duration_options,
                android.R.layout.simple_spinner_item
        );
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceDurationSpinner.setAdapter(durationAdapter);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Add Event")
                .setView(dialogView)
                .setPositiveButton("Add", null) // we override later so it won't auto-dismiss
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(v -> {
                String title = titleInput.getText().toString().trim();

                // validate required fields
                boolean valid = true;

                if (title.isEmpty()) {
                    titleInput.setError("Title is required");
                    valid = false;
                }

                if (selectedDateMillis[0] == 0) {
                    dateText.setError("Please select a date");
                    valid = false;
                } else {
                    dateText.setError(null); // clear error
                }

                if (!valid) return; // don't dismiss if invalid

                // apply default values
                String desc = descriptionInput.getText().toString().trim();
                String category = categoryInput.getText().toString().trim();
                String recurrence = recurrenceSpinner.getSelectedItem().toString();
                boolean important = importantCheckbox.isChecked();

                if (desc.isEmpty()) desc = "";
                if (category.isEmpty()) category = "General";
                if (recurrence.isEmpty()) recurrence = "None";

                int reminder = 0;
                String reminderStr = reminderInput.getText().toString().trim();
                if (!reminderStr.isEmpty()) {
                    try {
                        reminder = Integer.parseInt(reminderStr);
                    } catch (NumberFormatException e) {
                        reminder = 0;
                    }
                }

                int repeatMonths = 1;
                String durationSelected = recurrenceDurationSpinner.getSelectedItem().toString();
                switch (durationSelected) {
                    case "3 months": repeatMonths = 3; break;
                    case "6 months": repeatMonths = 6; break;
                    case "12 months": repeatMonths = 12; break;
                }

                // create and save event
                Event event = new Event(
                        0, title, desc, selectedDateMillis[0],
                        category, reminder, recurrence, important
                );
                db.insertEvent(event, repeatMonths);
                categorySet.add(category);
                updateCategorySpinner();
                refreshEventList();

                dialog.dismiss(); // now we close it manually
            });
        });

        dialog.show();
    }

    private void refreshEventList() {
        ArrayList<Event> events = db.getUpcomingEvents(Integer.MAX_VALUE);
        adapter = new EventAdapter(requireContext(), events);
        recyclerView.setAdapter(adapter);

        if (toggleButton.isChecked()) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
    }
}
