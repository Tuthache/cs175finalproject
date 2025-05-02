package edu.sjsu.android.cs175finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.sjsu.android.cs175finalproject.Event;
import edu.sjsu.android.cs175finalproject.R;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView, dateView;

        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.event_title);
            dateView = view.findViewById(R.id.event_date);
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
        Event e = events.get(position);
        holder.titleView.setText(e.getTitle());
        holder.dateView.setText(new java.util.Date(e.getDateMillis()).toString());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
