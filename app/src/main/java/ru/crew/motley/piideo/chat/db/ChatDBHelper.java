package ru.crew.motley.piideo.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ru.crew.motley.piideo.chat.db.PiideoSchema.ChatTable;

/**
 * Created by vas on 12/26/17.
 */

public class ChatDBHelper extends SQLiteOpenHelper {

    public static final int VERSION = 1;

    private static final String DATABASE_NAME = "chatBase.db";

    public ChatDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ChatTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                ChatTable.Cols.UUID + ", " +
                ChatTable.Cols.PIIDEO_FILE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
