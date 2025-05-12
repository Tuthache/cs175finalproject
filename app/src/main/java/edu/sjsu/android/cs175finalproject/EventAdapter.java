package edu.sjsu.android.cs175finalproject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

// adapter that controls how events are displayed in the recyclerview
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> implements Filterable {

    private final Context context;
    private final List<Event> allEvents;
    private List<Event> filteredEvents;
    private OnEventActionListener eventActionListener;

    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    // we call this to let the fragment react to edit/delete
    public void setOnEventActionListener(OnEventActionListener listener) {
        this.eventActionListener = listener;
    }

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

    // defines what views each event card uses
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleView, dateView, categoryView, importantView;
        public ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.event_title);
            dateView = view.findViewById(R.id.event_date);
            categoryView = view.findViewById(R.id.event_category);
            importantView = view.findViewById(R.id.event_important);
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
        holder.dateView.setText(new java.text.SimpleDateFormat("EEE, MMM d 'at' h:mm a", Locale.getDefault())
                .format(new java.util.Date(e.getDateMillis())));
        holder.categoryView.setText("Category: " + e.getCategory());

        if (e.isImportant()) {
            holder.importantView.setVisibility(View.VISIBLE);
        } else {
            holder.importantView.setVisibility(View.GONE);
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

    // lets us search through titles, descriptions, and categories
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

    // handles swiping left/right to delete or edit
    public ItemTouchHelper.SimpleCallback getSwipeCallback() {
        return new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            private final float swipeThreshold = 0.8f;

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return swipeThreshold;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Event event = filteredEvents.get(position);
                if (direction == ItemTouchHelper.LEFT && eventActionListener != null) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete Event")
                            .setMessage("Are you sure you want to delete this event?")
                            .setPositiveButton("Delete", (dialog, which) -> eventActionListener.onDelete(event))
                            .setNegativeButton("Cancel", (dialog, which) -> {
                                new android.os.Handler().post(() -> notifyItemChanged(position));
                            })
                            .show();
                } else if (direction == ItemTouchHelper.RIGHT && eventActionListener != null) {
                    // Launch edit dialog similar to add
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_event, null);
                    EditText titleInput = dialogView.findViewById(R.id.input_title);
                    EditText descriptionInput = dialogView.findViewById(R.id.input_description);
                    Button dateButton = dialogView.findViewById(R.id.button_pick_date);
                    TextView dateText = dialogView.findViewById(R.id.selected_date);
                    EditText categoryInput = dialogView.findViewById(R.id.input_category);
                    EditText reminderInput = dialogView.findViewById(R.id.input_reminder);
                    Spinner recurrenceSpinner = dialogView.findViewById(R.id.spinner_recurrence);
                    // set up spinner with recurrence options
                    ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(
                            context,
                            R.array.recurrence_options, // defined in strings.xml
                            android.R.layout.simple_spinner_item);
                    recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    recurrenceSpinner.setAdapter(recurrenceAdapter);
                    CheckBox importantCheckbox = dialogView.findViewById(R.id.checkbox_important);

                    // Pre-fill existing values
                    titleInput.setText(event.getTitle());
                    descriptionInput.setText(event.getDescription());
                    dateText.setText(new java.util.Date(event.getDateMillis()).toString());
                    categoryInput.setText(event.getCategory());
                    reminderInput.setText(String.valueOf(event.getReminderMinutes()));
                    int selectedIndex = 0;
                    switch (event.getRecurrence()) {
                        case "Weekly": selectedIndex = 1; break;
                        case "Monthly": selectedIndex = 2; break;
                    }
                    recurrenceSpinner.setSelection(selectedIndex);
                    importantCheckbox.setChecked(event.isImportant());
                    final long[] selectedDateMillis = {event.getDateMillis()};

                    dateButton.setOnClickListener(view -> {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(selectedDateMillis[0]);
                        DatePickerDialog dpd = new DatePickerDialog(context,
                                (picker, year, month, day) -> {
                                    Calendar chosen = Calendar.getInstance();
                                    chosen.set(year, month, day);
                                    selectedDateMillis[0] = chosen.getTimeInMillis();
                                    dateText.setText(new java.util.Date(selectedDateMillis[0]).toString());
                                },
                                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                        dpd.show();
                    });

                    new AlertDialog.Builder(context)
                            .setTitle("Edit Event")
                            .setView(dialogView)
                            .setPositiveButton("Save", (dialog, which) -> {
                                event.setTitle(titleInput.getText().toString());
                                event.setDescription(descriptionInput.getText().toString());
                                event.setDateMillis(selectedDateMillis[0]);
                                event.setCategory(categoryInput.getText().toString());
                                event.setReminderMinutes(Integer.parseInt(reminderInput.getText().toString()));
                                event.setRecurrence(recurrenceSpinner.getSelectedItem().toString());
                                event.setImportant(importantCheckbox.isChecked());
                                if (eventActionListener != null) eventActionListener.onEdit(event);
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> notifyItemChanged(position))
                            .show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                Paint paint = new Paint();
                float width = itemView.getWidth();
                float swipeRatio = Math.min(Math.abs(dX) / width, 1.0f);

                if (dX > 0) {
                    int baseColor = Color.parseColor("#2196F3"); // Blue
                    int alpha = (int)(swipeRatio * 255);
                    paint.setColor(baseColor);
                    paint.setAlpha(alpha);
                    c.drawRect(itemView.getLeft(), itemView.getTop(), dX,
                            itemView.getBottom(), paint);

                    Drawable icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_edit);
                    if (icon != null) {
                        int iconSize = 80;
                        int top = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                        int left = itemView.getLeft() + 50;
                        icon.setBounds(left, top, left + iconSize, top + iconSize);
                        icon.setAlpha(alpha);
                        icon.draw(c);
                    }
                } else if (dX < 0) {
                    int baseColor = Color.parseColor("#F44336"); // Red
                    int alpha = (int)(swipeRatio * 255);
                    paint.setColor(baseColor);
                    paint.setAlpha(alpha);
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom(), paint);

                    Drawable icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete);
                    if (icon != null) {
                        int iconSize = 80;
                        int top = itemView.getTop() + (itemView.getHeight() - iconSize) / 2;
                        int right = itemView.getRight() - 50;
                        icon.setBounds(right - iconSize, top, right, top + iconSize);
                        icon.setAlpha(alpha);
                        icon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }



        };
    }
}