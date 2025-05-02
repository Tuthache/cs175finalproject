package edu.sjsu.android.cs175finalproject;

public class Event {
    private int id; // unique event id
    private String title;
    private String description;
    private long dateMillis;

    public Event(int id, String title, String description, long dateMillis) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateMillis = dateMillis;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getDateMillis() { return dateMillis; }
}
