package edu.sjsu.android.cs175finalproject;

import static edu.sjsu.android.cs175finalproject.EventDBSchema.EventTable.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;

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

    public long insertEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, event.getTitle());
        values.put(DESCRIPTION, event.getDescription());
        values.put(DATE, event.getDateMillis());
        values.put(CATEGORY, event.getCategory());
        values.put(REMINDER, event.getReminderMinutes());
        values.put(RECURRENCE, event.getRecurrence());
        values.put(IMPORTANT, event.isImportant() ? 1 : 0);
        return db.insert(TABLE_NAME, null, values);
    }


    public ArrayList<Event> getUpcomingEvents(int limit) {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DATE + " ASC",
                limit > 0 ? String.valueOf(limit) : null //null returns all upcoming events
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


    public void deleteEvent(int eventId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, ID + " = ?", new String[]{String.valueOf(eventId)});
    }
}
