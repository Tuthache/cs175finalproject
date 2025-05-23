package edu.sjsu.android.cs175finalproject.ui.event;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
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
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.sjsu.android.cs175finalproject.Event;
import edu.sjsu.android.cs175finalproject.EventAdapter;
import edu.sjsu.android.cs175finalproject.EventDatabase;
import edu.sjsu.android.cs175finalproject.EventReminderReceiver;
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

        FloatingActionButton micFab = root.findViewById(R.id.fab_voice_input);
        micFab.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.RECORD_AUDIO}, 102);
            } else {
                startVoiceInput(); // we already have permission
            }
        });

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
        Calendar eventCalendar = Calendar.getInstance();
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
        final boolean[] datePicked = {false};
        final boolean[] timePicked = {false};
        Button timeButton = dialogView.findViewById(R.id.button_pick_time);
        TextView timeText = dialogView.findViewById(R.id.selected_time);

        final long[] selectedDateMillis = {0};

        dateButton.setOnClickListener(view -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(getContext(),
                    (datePicker, year, month, day) -> {
                        eventCalendar.set(Calendar.YEAR, year);
                        eventCalendar.set(Calendar.MONTH, month);
                        eventCalendar.set(Calendar.DAY_OF_MONTH, day);
                        datePicked[0] = true; // mark date as picked
                        dateText.setText(new Date(eventCalendar.getTimeInMillis()).toString());
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        timeButton.setOnClickListener(view -> {
            int hour = eventCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = eventCalendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (timePicker, selectedHour, selectedMinute) -> {
                        eventCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        eventCalendar.set(Calendar.MINUTE, selectedMinute);
                        timePicked[0] = true; // mark time as picked
                        timeText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                    },
                    hour, minute, false);
            timePickerDialog.show();
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

                if (!datePicked[0]) {
                    dateText.setError("Please select a date");
                    valid = false;
                } else {
                    dateText.setError(null);
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
                        0, title, desc, eventCalendar.getTimeInMillis(),
                        category, reminder, recurrence, important
                );
                db.insertEvent(event, repeatMonths);

                if (event.getReminderMinutes() > 0) {
                    scheduleReminder(requireContext(), event);
                }

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

    public static void scheduleReminder(Context context, Event event) {
        Intent intent = new Intent(context, EventReminderReceiver.class);
        intent.putExtra("title", "Upcoming Event: " + event.getTitle());
        intent.putExtra("message", "Starts at " +
                new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(event.getDateMillis())));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, event.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        long reminderTime = event.getDateMillis() - (event.getReminderMinutes() * 60 * 1000L);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
    }

    private static final int VOICE_REQUEST_CODE = 101;

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your event...");

        try {
            startActivityForResult(intent, VOICE_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Your device doesn't support voice input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String input = result.get(0);
                parseVoiceInput(input);
            }
        }
    }

    private void parseVoiceInput(String input) {
        String title = "Untitled";
        long millis = System.currentTimeMillis();

        Pattern pattern = Pattern.compile("called (.*?) on (.*?) at (.*?)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            title = matcher.group(1).trim();
            String dateStr = matcher.group(2).trim();
            String timeStr = matcher.group(3).trim();

            try {
                SimpleDateFormat df = new SimpleDateFormat("MMMM d yyyy h:mm a", Locale.US);
                Date fullDate = df.parse(dateStr + " " + timeStr);
                if (fullDate != null) {
                    millis = fullDate.getTime();
                }
            } catch (ParseException e) {
                Log.e("VoiceParse", "Date parse failed: " + e.getMessage());
            }
        }

        showPrefilledAddEventDialog(title, millis);
    }

    private void showPrefilledAddEventDialog(String title, long dateMillis) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);

        EditText titleInput = dialogView.findViewById(R.id.input_title);
        TextView dateText = dialogView.findViewById(R.id.selected_date);
        Button dateButton = dialogView.findViewById(R.id.button_pick_date);
        Button timeButton = dialogView.findViewById(R.id.button_pick_time);
        TextView timeText = dialogView.findViewById(R.id.selected_time);
        EditText descriptionInput = dialogView.findViewById(R.id.input_description);
        EditText categoryInput = dialogView.findViewById(R.id.input_category);
        EditText reminderInput = dialogView.findViewById(R.id.input_reminder);
        CheckBox importantCheckbox = dialogView.findViewById(R.id.checkbox_important);
        Spinner recurrenceSpinner = dialogView.findViewById(R.id.spinner_recurrence);
        Spinner durationSpinner = dialogView.findViewById(R.id.spinner_recurrence_duration);

        // setup spinners
        ArrayAdapter<CharSequence> recAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.recurrence_options, android.R.layout.simple_spinner_item);
        recAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceSpinner.setAdapter(recAdapter);

        ArrayAdapter<CharSequence> durAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.recurrence_duration_options, android.R.layout.simple_spinner_item);
        durAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(durAdapter);

        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTimeInMillis(dateMillis);
        titleInput.setText(title);
        dateText.setText(new Date(dateMillis).toString());
        timeText.setText(String.format(Locale.getDefault(), "%02d:%02d",
                eventCalendar.get(Calendar.HOUR_OF_DAY),
                eventCalendar.get(Calendar.MINUTE)));

        // date picker
        dateButton.setOnClickListener(v -> {
            DatePickerDialog dpd = new DatePickerDialog(getContext(),
                    (view, year, month, day) -> {
                        eventCalendar.set(Calendar.YEAR, year);
                        eventCalendar.set(Calendar.MONTH, month);
                        eventCalendar.set(Calendar.DAY_OF_MONTH, day);
                        dateText.setText(new Date(eventCalendar.getTimeInMillis()).toString());
                    },
                    eventCalendar.get(Calendar.YEAR),
                    eventCalendar.get(Calendar.MONTH),
                    eventCalendar.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        // time picker
        timeButton.setOnClickListener(view -> {
            int hour = eventCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = eventCalendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (timePicker, selectedHour, selectedMinute) -> {
                        eventCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        eventCalendar.set(Calendar.MINUTE, selectedMinute);
                        timeText.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                    },
                    hour, minute, false);
            timePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Add Event from Voice")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String desc = descriptionInput.getText().toString();
                    String category = categoryInput.getText().toString();
                    String recurrence = recurrenceSpinner.getSelectedItem().toString();
                    boolean important = importantCheckbox.isChecked();

                    int reminder = 0;
                    try {
                        reminder = Integer.parseInt(reminderInput.getText().toString());
                    } catch (Exception ignored) {}

                    int repeatMonths = 1;
                    String durStr = durationSpinner.getSelectedItem().toString();
                    switch (durStr) {
                        case "3 months": repeatMonths = 3; break;
                        case "6 months": repeatMonths = 6; break;
                        case "12 months": repeatMonths = 12; break;
                    }

                    Event event = new Event(0, title, desc, eventCalendar.getTimeInMillis(),
                            category, reminder, recurrence, important);
                    db.insertEvent(event, repeatMonths);
                    refreshEventList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput(); // now we can safely start it
            } else {
                Toast.makeText(getContext(), "Microphone permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
