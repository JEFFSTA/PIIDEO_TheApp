package ru.crew.motley.piideo.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.School;
import ru.crew.motley.piideo.network.Subject;

/**
 * Created by vas on 12/26/17.
 */

public class ChatLab {

    private static ChatLab sEventLab;

    private Context mContext;
    private SQLiteDatabase mDataBase;


    public static ChatLab get(Context context) {
        if (sEventLab == null) {
            sEventLab = new ChatLab(context);
        }
        return sEventLab;
    }

    public static ContentValues getContentValues(PiideoRow row) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.ChatTable.Cols.PIIDEO_FILE, row.getPiideoFileName());
        values.put(PiideoSchema.ChatTable.Cols.PIIDEO_STATE, row.getPiideoState());
        return values;
    }

    public static ContentValues getContentValues(Member member) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.MemberTable.Cols.NEO_ID, member.getId());
        values.put(PiideoSchema.MemberTable.Cols.PHONE, member.getPhoneNumber());
        values.put(PiideoSchema.MemberTable.Cols.CHAT_ID, member.getChatId());
        values.put(PiideoSchema.MemberTable.Cols.C_CODE, member.getCountryCode());
        values.put(PiideoSchema.MemberTable.Cols.PH_PREFIX, member.getPhonePrefix());
        return values;
    }

    public static ContentValues getContentValues(Subject subject) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.SubjectTable.Cols.NEO_ID, subject.getId());
        values.put(PiideoSchema.SubjectTable.Cols.NAME, subject.getName());
        return values;
    }

    public static ContentValues getContentValues(School school) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.SchoolTable.Cols.NEO_ID, school.getId());
        values.put(PiideoSchema.SchoolTable.Cols.NAME, school.getName());
        return values;
    }

    public static ContentValues getContentValues(FcmMessage message) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.MessageTable.Cols.CONTENT, message.getContent());
        values.put(PiideoSchema.MessageTable.Cols.SENDER_ID, message.getFrom());
        values.put(PiideoSchema.MessageTable.Cols.RECEIVER_ID, message.getTo());
        values.put(PiideoSchema.MessageTable.Cols.MSG_TYPE, message.getType());
        return values;
    }

    public ChatLab(Context context) {
        mContext = context;
        mDataBase = new ChatDBHelper(mContext).getWritableDatabase();
//        mCrimes = new ArrayList<>();
    }

    private ChatRowCursorWrapper queryChat(String whereClause, String[] whereArgs) {
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

    private SubjectCursorWrapper querySubject(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                PiideoSchema.SubjectTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new SubjectCursorWrapper(cursor);
    }

    private SchoolCursorWrapper querySchool(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                PiideoSchema.SchoolTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new SchoolCursorWrapper(cursor);
    }

    private MemberCursorWrapper queryMember(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                PiideoSchema.MemberTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new MemberCursorWrapper(cursor);
    }

    private FcmMessageCursorWrapper queryMessage(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                PiideoSchema.MessageTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);
        return new FcmMessageCursorWrapper(cursor);
    }

    private <W> W query0(String whereClause, String[] whereArgs, WrapperCreator<W> creator) {
        Cursor cursor = mDataBase.query(
                PiideoSchema.MessageTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);
        return creator.create(cursor);
    }

    @FunctionalInterface
    interface WrapperCreator<T> {
        T create(Cursor cursor);
    }

    public List<PiideoRow> getPiideos() {
        List<PiideoRow> piideos = new ArrayList<>();
        ChatRowCursorWrapper cursor = queryChat(null, null);
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

    public PiideoRow searchBy(String piideoName) {
        try (ChatRowCursorWrapper cursor = queryChat(
                PiideoSchema.ChatTable.Cols.PIIDEO_FILE + " = ?",
                new String[]{piideoName}
        )) {
            if (cursor.getCount() > 1) {
                throw new RuntimeException("More then one piideo with same name " + piideoName);
            }
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPiideoRow();
        }
    }

    public void setPiideoDone(String piideoName) {
        ContentValues values = new ContentValues();
        values.put(PiideoSchema.ChatTable.Cols.PIIDEO_STATE, PiideoSchema.ChatTable.PIIDEO_STATE_DONE);
        mDataBase.update(
                PiideoSchema.ChatTable.NAME,
                values,
                PiideoSchema.ChatTable.Cols.PIIDEO_FILE + " = ?",
                new String[]{piideoName});
    }

    public Member getMember() {
        Subject subject = getSubject();
        School school = getSchool();
        try (MemberCursorWrapper cursor = queryMember(null, null)) {
            if (cursor.getCount() > 1) {
                throw new RuntimeException("More then one Member in DB");
            }
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getMember(subject, school);
        }
    }

    private Subject getSubject() {
        try (SubjectCursorWrapper cursor = querySubject(null, null)) {
            if (cursor.getCount() > 1) {
                throw new RuntimeException("More then one subject in DB.");
            }
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getSubject();
        }
    }

    private School getSchool() {
        try (SchoolCursorWrapper cursor = querySchool(null, null)) {
            if (cursor.getCount() > 1) {
                throw new RuntimeException("More then one School in DB.");
            }
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getSchool();
        }
    }

    public void addMember(Member member) {
        addSchool(member.getSchool());
        addSubject(member.getSubject());
        ContentValues values = ChatLab.getContentValues(member);
        mDataBase.insert(PiideoSchema.MemberTable.NAME, null, values);
    }

    private void addSubject(Subject subject) {
        ContentValues values = ChatLab.getContentValues(subject);
        mDataBase.insert(PiideoSchema.SubjectTable.NAME, null, values);
    }

    private void addSchool(School school) {
        ContentValues values = ChatLab.getContentValues(school);
        mDataBase.insert(PiideoSchema.SchoolTable.NAME, null, values);
    }

    public void deleteMember() {
        deleteSubject();
        deleteSchool();
        mDataBase.delete(PiideoSchema.MemberTable.NAME, null, null);
    }

    private void deleteSubject() {
        mDataBase.delete(PiideoSchema.SubjectTable.NAME, null, null);
    }

    private void deleteSchool() {
        mDataBase.delete(PiideoSchema.SchoolTable.NAME, null, null);
    }

    public long addMessage(FcmMessage message) {
        ContentValues values = ChatLab.getContentValues(message);
        return mDataBase.insert(PiideoSchema.MessageTable.NAME, null, values);
    }

    public FcmMessage getReducedFcmMessage(String id) {
        try (
                FcmMessageCursorWrapper cursor = queryMessage(
                        PiideoSchema.MessageTable.Cols.UUID + " = ? ",
                        new String[]{id})) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getFcmMessage();
        }
    }

}
