package edu.sjsu.android.cs175finalproject;

import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.CATEGORY;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.DATE;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.DESCRIPTION;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.ID;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.IMPORTANT;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.RECURRENCE;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.REMINDER;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.TABLE_NAME;
import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.TITLE;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;

// helper class to create/manage our sqlite event database
public class EventDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eventDatabase";
    private static final int VERSION = 1;

    public EventDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final String CREATE_EVENTS_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    TITLE + " TEXT, " +
                    DESCRIPTION + " TEXT, " +
                    DATE + " INTEGER, " +
                    CATEGORY + " TEXT, " +
                    REMINDER + " INTEGER, " +
                    RECURRENCE + " TEXT, " +
                    IMPORTANT + " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertEvent(Event event, int repeatMonths) {
        SQLiteDatabase db = getWritableDatabase();
        insertSingleEvent(db, event);

        String recurrence = event.getRecurrence();
        if (recurrence != null && !recurrence.equalsIgnoreCase("None")) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(event.getDateMillis());

            Calendar end = Calendar.getInstance();
            end.setTimeInMillis(event.getDateMillis());
            end.add(Calendar.MONTH, repeatMonths);

            while (calendar.before(end)) {
                if (recurrence.equalsIgnoreCase("Weekly")) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                } else if (recurrence.equalsIgnoreCase("Monthly")) {
                    calendar.add(Calendar.MONTH, 1);
                } else {
                    break;
                }

                Event repeated = new Event(
                        0, event.getTitle(), event.getDescription(),
                        calendar.getTimeInMillis(), event.getCategory(),
                        event.getReminderMinutes(), "None", event.isImportant()
                );
                insertSingleEvent(db, repeated);
            }
        }

    }

    // helper method to reduce code duplication
    private void insertSingleEvent(SQLiteDatabase db, Event event) {
        ContentValues values = new ContentValues();
        values.put(EventDBSchema.EventTable.TITLE, event.getTitle());
        values.put(EventDBSchema.EventTable.DESCRIPTION, event.getDescription());
        values.put(EventDBSchema.EventTable.DATE, event.getDateMillis());
        values.put(EventDBSchema.EventTable.CATEGORY, event.getCategory());
        values.put(EventDBSchema.EventTable.REMINDER, event.getReminderMinutes());
        values.put(EventDBSchema.EventTable.RECURRENCE, event.getRecurrence());
        values.put(EventDBSchema.EventTable.IMPORTANT, event.isImportant() ? 1 : 0);
        db.insert(EventDBSchema.EventTable.TABLE_NAME, null, values);
    }

    public ArrayList<Event> getUpcomingEvents(int limit) {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        long currentTimeMillis = System.currentTimeMillis();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                DATE + " >= ?",
                new String[]{String.valueOf(currentTimeMillis)},
                null,
                null,
                DATE + " ASC",
                limit > 0 ? String.valueOf(limit) : null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION));
            long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DATE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY));
            int reminder = cursor.getInt(cursor.getColumnIndexOrThrow(REMINDER));
            String recurrence = cursor.getString(cursor.getColumnIndexOrThrow(RECURRENCE));
            boolean important = cursor.getInt(cursor.getColumnIndexOrThrow(IMPORTANT)) == 1;

            Event event = new Event(id, title, description, dateMillis, category, reminder, recurrence, important);
            events.add(event);
        }

        cursor.close();
        return events;
    }

    public void updateEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, event.getTitle());
        values.put(DESCRIPTION, event.getDescription());
        values.put(DATE, event.getDateMillis());
        values.put(CATEGORY, event.getCategory());
        values.put(REMINDER, event.getReminderMinutes());
        values.put(RECURRENCE, event.getRecurrence());
        values.put(IMPORTANT, event.isImportant() ? 1 : 0);

        db.update(TABLE_NAME, values,
                ID + " = ?",
                new String[]{String.valueOf(event.getId())});
        db.close();
    }

    public void deleteEvent(int eventId) {
        // remove event from database using its id

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(eventId)});
    }

    public ArrayList<Event> getPastEvents(int limit) {
        // get past events sorted by date (most recent first)
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        long currentTimeMillis = System.currentTimeMillis();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                DATE + " < ?",
                new String[]{String.valueOf(currentTimeMillis)},
                null,
                null,
                DATE + " DESC",
                limit > 0 ? String.valueOf(limit) : null
        );

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION));
            long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DATE));
            String category = cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY));
            int reminder = cursor.getInt(cursor.getColumnIndexOrThrow(REMINDER));
            String recurrence = cursor.getString(cursor.getColumnIndexOrThrow(RECURRENCE));
            boolean important = cursor.getInt(cursor.getColumnIndexOrThrow(IMPORTANT)) == 1;

            Event event = new Event(id, title, description, dateMillis, category, reminder, recurrence, important);
            events.add(event);
        }

        cursor.close();
        return events;
    }

}
