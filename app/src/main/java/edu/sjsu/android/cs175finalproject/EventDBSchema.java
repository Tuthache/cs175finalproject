package edu.sjsu.android.cs175finalproject;

public final class EventDBSchema {

    // defines constants for sqlite table n column names to keep consistent
    private EventDBSchema() {}

    public static class EventTable {
        public static final String TABLE_NAME = "events";

        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String DATE = "date";
        public static final String CATEGORY = "category";
        public static final String REMINDER = "reminder_minutes";
        public static final String RECURRENCE = "recurrence";
        public static final String IMPORTANT = "important";  // 0 or 1
    }
}
