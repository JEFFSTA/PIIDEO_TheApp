package ru.crew.motley.piideo.chat.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ru.crew.motley.piideo.network.Member;

import static ru.crew.motley.piideo.chat.db.PiideoSchema.ChatTable;
import static ru.crew.motley.piideo.chat.db.PiideoSchema.MemberTable;
import static ru.crew.motley.piideo.chat.db.PiideoSchema.SchoolTable;
import static ru.crew.motley.piideo.chat.db.PiideoSchema.SubjectTable;
import static ru.crew.motley.piideo.chat.db.PiideoSchema.MessageTable;
import static ru.crew.motley.piideo.chat.db.PiideoSchema.MemberQueue;

public class ChatDBHelper extends SQLiteOpenHelper {

    //    public static final int VERSION = 1;
    public static final int VERSION = 2;

    private static final String DATABASE_NAME = "chatBase.db";

    public ChatDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ChatTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                ChatTable.Cols.UUID + ", " +
                ChatTable.Cols.PIIDEO_FILE + ", " +
                ChatTable.Cols.PIIDEO_STATE +
                ")"
        );

        db.execSQL("create table " + MemberTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                MemberTable.Cols.NEO_ID + " TEXT, " +
                MemberTable.Cols.PHONE + " TEXT," +
                MemberTable.Cols.CHAT_ID + " TEXT, " +
                MemberTable.Cols.C_CODE + " TEXT, " +
                MemberTable.Cols.PH_PREFIX + " TEXT)");

        db.execSQL("create table " + SchoolTable.NAME + "(" +
                SchoolTable.Cols.NEO_ID + " TEXT primary key, " +
                SchoolTable.Cols.NAME + " TEXT)");

        db.execSQL("create table " + SubjectTable.NAME + "(" +
                SubjectTable.Cols.NEO_ID + " TEXT primary key, " +
                SubjectTable.Cols.NAME + " TEXT)");

        db.execSQL("create table " + MessageTable.NAME + "(" +
                MessageTable.Cols.UUID + " integer primary key autoincrement, " +
                MessageTable.Cols.CONTENT + " TEXT, " +
                MessageTable.Cols.SENDER_ID + " TEXT, " +
                MessageTable.Cols.RECEIVER_ID + " TEXT, " +
                MessageTable.Cols.MSG_TYPE + " TEXT, " +
                MessageTable.Cols.TIMESTAMP + "TEXT)");

        db.execSQL("create table " + MemberQueue.NAME + "(" +
                " _id integer primary key autoincrement, " +
                MemberQueue.Cols.NEO_ID + " TEXT," +
                MemberQueue.Cols.PHONE_NUM + " TEXT, " +
                MemberQueue.Cols.PHONE_PR + " TEXT, " +
                MemberQueue.Cols.CHAT_ID + " TEXT," +
                MemberQueue.Cols.C_CODE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.execSQL("alter table " + MessageTable.NAME +
                    " add column " + MessageTable.Cols.TIMESTAMP + " TEXT");
        }
    }
}
