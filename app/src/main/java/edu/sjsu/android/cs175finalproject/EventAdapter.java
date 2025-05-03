package edu.sjsu.android.cs175finalproject;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private final List<Event> allEvents;
    private List<Event> filteredEvents;

    public EventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.allEvents = new ArrayList<>(events);
        this.filteredEvents = new ArrayList<>(events);
        sortByImportance();
    }

    private void sortByImportance() {
        Collections.sort(filteredEvents, (e1, e2) -> Boolean.compare(!e1.isImportant(), !e2.isImportant()));
    }

    public void filterByCategory(String category) {
        filteredEvents.clear();
        for (Event e : allEvents) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                filteredEvents.add(e);
            }
        }
        sortByImportance();
        notifyDataSetChanged();
    }

    public void filterByImportantOnly() {
        filteredEvents.clear();
        for (Event e : allEvents) {
            if (e.isImportant()) {
                filteredEvents.add(e);
            }
        }
        sortByImportance();
        notifyDataSetChanged();
    }

    public void sortByTimeAscending() {
        filteredEvents.sort(Comparator.comparingLong(Event::getDateMillis));
        notifyDataSetChanged();
    }

    public void sortByTimeDescending() {
        filteredEvents.sort((e1, e2) -> Long.compare(e2.getDateMillis(), e1.getDateMillis()));
        notifyDataSetChanged();
    }

    public void resetFilters() {
        filteredEvents = new ArrayList<>(allEvents);
        sortByImportance();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView, dateView, categoryView;
        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.event_title);
            dateView = view.findViewById(R.id.event_date);
            categoryView = view.findViewById(R.id.event_category);
        }
    }

    @NonNull
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event e = filteredEvents.get(position);
        holder.titleView.setText(e.getTitle());
        holder.dateView.setText(new java.util.Date(e.getDateMillis()).toString());
        holder.categoryView.setText("Category: " + e.getCategory());

        if (e.isImportant()) {
            holder.itemView.setBackgroundColor(0xFFFFE0B2);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
        }

        holder.itemView.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(e.getTitle())
                    .setMessage("Date: " + new java.util.Date(e.getDateMillis()).toString() +
                            "\nCategory: " + e.getCategory() +
                            "\nReminder: " + e.getReminderMinutes() + " min before" +
                            "\nRecurrence: " + e.getRecurrence() +
                            "\n\n" + e.getDescription())
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return filteredEvents.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Event> filtered = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filtered.addAll(allEvents);
                } else {
                    String filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim();
                    for (Event e : allEvents) {
                        if (e.getTitle().toLowerCase(Locale.ROOT).contains(filterPattern) ||
                                e.getDescription().toLowerCase(Locale.ROOT).contains(filterPattern) ||
                                e.getCategory().toLowerCase(Locale.ROOT).contains(filterPattern)) {
                            filtered.add(e);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredEvents.clear();
                filteredEvents.addAll((List<Event>) results.values);
                sortByImportance();
                notifyDataSetChanged();
            }
        };
    }
}
