package edu.sjsu.android.cs175finalproject.ui.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.*;
import java.time.*;
import java.util.*;

import edu.sjsu.android.cs175finalproject.Event;
import edu.sjsu.android.cs175finalproject.EventDatabase;
import edu.sjsu.android.cs175finalproject.R;

public class CalendarFragment extends Fragment {

    private MaterialCalendarView calendarView;
    private TextView selectedDateText;

    private EventDatabase eventDatabase;
    private Map<LocalDate, List<Event>> dateEventMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        selectedDateText = view.findViewById(R.id.selectedDateText);
        eventDatabase = new EventDatabase(getContext());

        loadEventsFromDatabase();

        // Highlight days with events
        for (LocalDate date : dateEventMap.keySet()) {
            CalendarDay day = CalendarDay.from(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            calendarView.addDecorator(new EventDecorator(Color.RED, Collections.singleton(day)));
        }

        // Handle date clicks
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget,
                                       @NonNull CalendarDay date, boolean selected) {
                LocalDate localDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                List<Event> events = dateEventMap.getOrDefault(localDate, new ArrayList<>());

                if (events.isEmpty()) {
                    selectedDateText.setText("No events for this date.");
                } else {
                    StringBuilder builder = new StringBuilder();
                    for (Event event : events) {
                        builder.append("• ").append(event.getTitle()).append("\n");
                    }
                    selectedDateText.setText(builder.toString());
                }
            }
        });

        return view;
    }

    private void loadEventsFromDatabase() {
        List<Event> allEvents = eventDatabase.getUpcomingEvents(0);

        for (Event event : allEvents) {
            Instant instant = Instant.ofEpochMilli(event.getDateMillis());
            LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            if (!dateEventMap.containsKey(date)) {
                dateEventMap.put(date, new ArrayList<>());
            }
            dateEventMap.get(date).add(event);
        }
    }
}