package edu.sjsu.android.cs175finalproject;

public class Event {
    private int id;
    private String title;
    private String description;
    private long dateMillis;
    private String category;
    private int reminderMinutes;
    private String recurrence;
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

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getDateMillis() { return dateMillis; }
    public String getCategory() { return category; }
    public int getReminderMinutes() { return reminderMinutes; }
    public String getRecurrence() { return recurrence; }
    public boolean isImportant() { return isImportant; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDateMillis(long dateMillis) { this.dateMillis = dateMillis; }
    public void setCategory(String category) { this.category = category; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }
    public void setRecurrence(String recurrence) { this.recurrence = recurrence; }
    public void setImportant(boolean important) { this.isImportant = important; }
}
