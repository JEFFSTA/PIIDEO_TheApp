package ru.crew.motley.piideo.chat.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.network.School;
import ru.crew.motley.piideo.network.Subject;

import static ru.crew.motley.piideo.chat.db.PiideoSchema.MemberTable;

/**
 * Created by vas on 1/5/18.
 */

public class MemberCursorWrapper extends CursorWrapper {

    public MemberCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Member getMember(Subject subject, School school) {
        int neo_id = getInt(getColumnIndex(MemberTable.Cols.NEO_ID));
        String phone = getString(getColumnIndex(MemberTable.Cols.PHONE));
        String chatId = getString(getColumnIndex(MemberTable.Cols.CHAT_ID));
        String countryCode = getString(getColumnIndex(MemberTable.Cols.C_CODE));
        String phonePrefix = getString(getColumnIndex(MemberTable.Cols.PH_PREFIX));
        Member row = new Member();
        row.setId((long) neo_id);
        row.setPhoneNumber(phone);
        row.setRegistered(true);
        row.setSubject(subject);
        row.setSchool(school);
        row.setChatId(chatId);
        row.setCountryCode(countryCode);
        row.setPhonePrefix(phonePrefix);
        return row;
    }
}
