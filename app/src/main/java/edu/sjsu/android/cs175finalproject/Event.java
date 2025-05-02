package edu.sjsu.android.cs175finalproject;

public class Event {
    private int id;
    private String title;
    private String description;
    private long dateMillis;
    private String category;       // ex: "Work", "School", "Personal"
    private int reminderMinutes;   // ex: remind me 30 mins before
    private String recurrence;     // "None", "Daily", "Weekly", "Monthly"
    private boolean isImportant;

    public Event(int id, String title, String description, long dateMillis,
                 String category, int reminderMinutes, String recurrence, boolean isImportant) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateMillis = dateMillis;
        this.category = category;
        this.reminderMinutes = reminderMinutes;
        this.recurrence = recurrence;
        this.isImportant = isImportant;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getDateMillis() { return dateMillis; }
    public String getCategory() { return category; }
    public int getReminderMinutes() { return reminderMinutes; }
    public String getRecurrence() { return recurrence; }
    public boolean isImportant() { return isImportant; }
}
