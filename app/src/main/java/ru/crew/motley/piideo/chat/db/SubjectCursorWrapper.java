package ru.crew.motley.piideo.chat.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import ru.crew.motley.piideo.network.Subject;

import static ru.crew.motley.piideo.chat.db.PiideoSchema.SubjectTable;

/**
 * Created by vas on 1/5/18.
 */

public class SubjectCursorWrapper extends CursorWrapper {

    public SubjectCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Subject getSubject() {
        int id = getInt(getColumnIndex(SubjectTable.Cols.NEO_ID));
        String name = getString(getColumnIndex(SubjectTable.Cols.NAME));
        Subject subject = new Subject();
        subject.setId((long) id);
        subject.setName(name);
        return subject;
    }
}
