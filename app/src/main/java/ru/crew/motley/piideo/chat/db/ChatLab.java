package ru.crew.motley.piideo.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vas on 12/26/17.
 */

public class ChatLab {

    private static ChatLab sEventLab;

    private static final boolean JOIN = true;
    private static final boolean NOT_JOIN = false;

    //    private List<Event> mCrimes;
    private Context mContext;
    private SQLiteDatabase mDataBase;


    public static ChatLab get(Context context) {
        if (sEventLab == null) {
            sEventLab = new ChatLab(context);
        }
        return sEventLab;
    }


//        private static ContentValues getContentValues(Event event) {
//            ContentValues values = new ContentValues();
//            values.put(EventTable.Cols.UUID, event.getId().toString());
//            values.put(EventTable.Cols.TITLE, event.getTitle());
//            values.put(EventTable.Cols.DATE, event.getDate().getTime());
//            values.put(EventTable.Cols.APPROVED, event.isApproved() ? 1 : 0);
//            values.put(EventTable.Cols.TEXT, event.getText());
////        values.put(EventTable.Cols.MESSAGE_ID, event.getMessageId());
//            return values;
//        }
//
//        private static ContentValues getContentValues(Participant user) {
//            ContentValues values = new ContentValues();
//            values.put(ParticipantTable.Cols.UUID, user.getId().toString());
//            values.put(ParticipantTable.Cols.NAME, user.getName());
//            values.put(ParticipantTable.Cols.CONTACT, user.getContact());
//            return values;
//        }
//
//        private static ContentValues getContentValues(UUID userId, String eventId) {
//            ContentValues values = new ContentValues();
//            values.put(JoinTable.Cols.PARTICIPANT_UUID, userId.toString());
//            values.put(JoinTable.Cols.UUID, eventId.toString());
//            return values;
//        }
//
//        private static ContentValues getCredentialsValues(Credentials credentials) {
//            ContentValues values = new ContentValues();
//            values.put(CredentialsTable.Cols.NAME, credentials.getEmail());
//            values.put(CredentialsTable.Cols.TOKEN, credentials.getToken());
//            values.put(CredentialsTable.Cols.HASH, credentials.getHash());
//            return values;
//        }

    public static ContentValues getContentValues(PiideoRow row) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.ChatTable.Cols.PIIDEO_FILE, row.getPiideoFileName());
        return values;
    }

    public ChatLab(Context context) {
        mContext = context;
        mDataBase = new ChatDBHelper(mContext).getWritableDatabase();
//        mCrimes = new ArrayList<>();
    }

    private ChatRowCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                PiideoSchema.ChatTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new ChatRowCursorWrapper(cursor);
    }

    public List<PiideoRow> getPiideos() {
        List<PiideoRow> piideos = new ArrayList<>();
        ChatRowCursorWrapper cursor = queryCrimes(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                piideos.add(cursor.getPiideoRow());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return piideos;
    }

    public void addPiideo(PiideoRow item) {
        ContentValues values = ChatLab.getContentValues(item);
        mDataBase.insert(PiideoSchema.ChatTable.NAME, null, values);
    }


}
