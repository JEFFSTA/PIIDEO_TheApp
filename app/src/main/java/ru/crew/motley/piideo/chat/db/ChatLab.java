package ru.crew.motley.piideo.chat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.crew.motley.piideo.fcm.FcmMessage;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.School;
import ru.crew.motley.piideo.network.Subject;

import static ru.crew.motley.piideo.chat.db.PiideoSchema.*;

/**
 * Created by vas on 12/26/17.
 */

public class ChatLab {

    private static ChatLab sEventLab;

    private Context mContext;
    private SQLiteDatabase mDataBase;


    public static ChatLab get(Context context) {
        if (sEventLab == null) {
            sEventLab = new ChatLab(context.getApplicationContext());
//            return new ChatLab(context.getApplicationContext());
        }
        return sEventLab;
    }

    public static ContentValues getContentValues(PiideoRow row) {
        ContentValues values = new ContentValues();
        values.put(ChatTable.Cols.PIIDEO_FILE, row.getPiideoFileName());
        values.put(ChatTable.Cols.PIIDEO_STATE, row.getPiideoState());
        return values;
    }

    public static ContentValues getContentValues(Member member) {
        ContentValues values = new ContentValues();
        values.put(MemberTable.Cols.NEO_ID, member.getId());
        values.put(MemberTable.Cols.PHONE, member.getPhoneNumber());
        values.put(MemberTable.Cols.CHAT_ID, member.getChatId());
        values.put(MemberTable.Cols.C_CODE, member.getCountryCode());
        values.put(MemberTable.Cols.PH_PREFIX, member.getPhonePrefix());
        return values;
    }

    public static ContentValues getContentValues(Subject subject) {
        ContentValues values = new ContentValues();
        values.put(SubjectTable.Cols.NEO_ID, subject.getId());
        values.put(SubjectTable.Cols.NAME, subject.getName());
        return values;
    }

    public static ContentValues getContentValues(School school) {
        ContentValues values = new ContentValues();
        values.put(SchoolTable.Cols.NEO_ID, school.getId());
        values.put(SchoolTable.Cols.NAME, school.getName());
        return values;
    }

    public static ContentValues getContentValues(FcmMessage message) {
        ContentValues values = new ContentValues();
        values.put(MessageTable.Cols.CONTENT, message.getContent());
        values.put(MessageTable.Cols.SENDER_ID, message.getFrom());
        values.put(MessageTable.Cols.RECEIVER_ID, message.getTo());
        values.put(MessageTable.Cols.MSG_TYPE, message.getType());
        values.put(MessageTable.Cols.TIMESTAMP, message.getTimestamp());
        return values;
    }

    public ChatLab(Context context) {
        mContext = context;
        mDataBase = new ChatDBHelper(mContext).getWritableDatabase();
//        mCrimes = new ArrayList<>();
    }

    private ChatRowCursorWrapper queryChat(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                ChatTable.NAME,
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
                SubjectTable.NAME,
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
                SchoolTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new SchoolCursorWrapper(cursor);
    }

    private MemberCursorWrapper queryMember(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDataBase.query(
                MemberTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        return new MemberCursorWrapper(cursor);
    }

    private MemberCursorWrapper queryQueueMember(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDataBase.query(
                MemberQueue.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        return new MemberCursorWrapper(cursor);
    }

    private FcmMessageCursorWrapper queryMessage(String whereClause, String[] whereArgs) {
        Cursor cursor = mDataBase.query(
                MessageTable.NAME,
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
                MessageTable.NAME,
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
        PiideoRow row = searchBy(item.getPiideoFileName());
        if (row != null) {
            Log.e("MULTIPLE", "Multiple piideo save");
        }
        ContentValues values = ChatLab.getContentValues(item);
        mDataBase.insert(ChatTable.NAME, null, values);
    }

    public void enqueue(List<Member> items) {
        List<ContentValues> cv = new ArrayList<>();
        for (Member member : items) {
            cv.add(ChatLab.getContentValues(member));
        }
//        mDataBase.beginTransaction();
        for (ContentValues values : cv) {
            long error = mDataBase.insertOrThrow(MemberQueue.NAME, null, values);
            Log.d("ENQUE", "" + error);
        }
//        mDataBase.endTransaction();
    }

    public Member pollNext() {
//        mDataBase.beginTransaction();
        Member member;
        try (MemberCursorWrapper cursor = queryQueueMember(null, null, null)) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            member = cursor.getMember(null, null);
        }
        mDataBase.delete(
                MemberQueue.NAME,
                MemberQueue.Cols.CHAT_ID + " = ?",
                new String[]{"" + member.getChatId()}
        );
//        mDataBase.endTransaction();
        return member;
    }

    public void clearQueue() {
        mDataBase.delete(MemberQueue.NAME, null, null);
    }

    public PiideoRow searchBy(String piideoName) {
        try (ChatRowCursorWrapper cursor = queryChat(
                ChatTable.Cols.PIIDEO_FILE + " = ?",
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
        values.put(ChatTable.Cols.PIIDEO_STATE, ChatTable.PIIDEO_STATE_DONE);
        mDataBase.update(
                ChatTable.NAME,
                values,
                ChatTable.Cols.PIIDEO_FILE + " = ?",
                new String[]{piideoName});
    }

//    public void setPiideoLoad(String piideoName) {
//        ContentValues values = new ContentValues();
//        values.put(ChatTable.Cols.PIIDEO_STATE, ChatTable.P)
//    }

    public Member getMember() {
        Subject subject = getSubject();
        School school = getSchool();
        try (MemberCursorWrapper cursor = queryMember(null, null, null)) {
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
        mDataBase.insert(MemberTable.NAME, null, values);
    }

    private void addSubject(Subject subject) {
        ContentValues values = ChatLab.getContentValues(subject);
        mDataBase.insert(SubjectTable.NAME, null, values);
    }

    private void addSchool(School school) {
        ContentValues values = ChatLab.getContentValues(school);
        mDataBase.insert(SchoolTable.NAME, null, values);
    }

    public void updateMember(Member member) {
        updateSchool(member.getSchool());
        updateSubject(member.getSubject());
        ContentValues values = ChatLab.getContentValues(member);
        mDataBase.update(
                MemberTable.NAME,
                values,
                MemberTable.Cols.CHAT_ID + " = ?",
                new String[]{member.getChatId()});
    }

    private void updateSubject(Subject subject) {
        deleteSubject();
        addSubject(subject);
    }

    private void updateSchool(School school) {
        deleteSchool();
        addSchool(school);
    }

    public void deleteMember() {
        deleteSubject();
        deleteSchool();
        mDataBase.delete(MemberTable.NAME, null, null);
    }

    private void deleteSubject() {
        mDataBase.delete(SubjectTable.NAME, null, null);
    }

    private void deleteSchool() {
        mDataBase.delete(SchoolTable.NAME, null, null);
    }

    public long addMessage(FcmMessage message) {
        ContentValues values = ChatLab.getContentValues(message);
        return mDataBase.insert(MessageTable.NAME, null, values);
    }

    public FcmMessage getReducedFcmMessage(String id) {
        try (
                FcmMessageCursorWrapper cursor = queryMessage(
                        MessageTable.Cols.UUID + " = ? ",
                        new String[]{id})) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getFcmMessage();
        }
    }

}
