package edu.sjsu.android.cs175finalproject;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private final List<Event> events;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault());

    public DashboardAdapter(List<Event> events) {
        this.events = events;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView, dateView, descriptionView, categoryView, reminderView, recurrenceView, importantView;

        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.dashboard_event_title);
            dateView = view.findViewById(R.id.dashboard_event_date);
            descriptionView = view.findViewById(R.id.dashboard_event_description);
            categoryView = view.findViewById(R.id.dashboard_event_category);
            reminderView = view.findViewById(R.id.dashboard_event_reminder);
            recurrenceView = view.findViewById(R.id.dashboard_event_recurrence);
            importantView = view.findViewById(R.id.dashboard_event_important);
        }
    }

    @NonNull
    @Override
    public DashboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_event, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event e = events.get(position);
        String formattedDate = dateFormat.format(new Date(e.getDateMillis()));

        // Set all event details
        holder.titleView.setText(e.getTitle());
        holder.dateView.setText(formattedDate);
        holder.descriptionView.setText(e.getDescription());
        holder.categoryView.setText("Category: " + e.getCategory());

        // Format reminder text
        String reminderText;
        if (e.getReminderMinutes() == 0) {
            reminderText = "No reminder";
        } else if (e.getReminderMinutes() == 60) {
            reminderText = "Reminder: 1 hour before";
        } else if (e.getReminderMinutes() > 60) {
            reminderText = "Reminder: " + (e.getReminderMinutes() / 60) + " hours before";
        } else {
            reminderText = "Reminder: " + e.getReminderMinutes() + " minutes before";
        }
        holder.reminderView.setText(reminderText);

        holder.recurrenceView.setText("Repeats: " + e.getRecurrence());

        // Show importance indicator if important
        if (e.isImportant()) {
            holder.importantView.setVisibility(View.VISIBLE);
            holder.importantView.setText("⭐ Important");
        } else {
            holder.importantView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}