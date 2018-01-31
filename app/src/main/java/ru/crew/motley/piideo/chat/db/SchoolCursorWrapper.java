package ru.crew.motley.piideo.chat.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import ru.crew.motley.piideo.network.School;

import static ru.crew.motley.piideo.chat.db.PiideoSchema.SchoolTable;

/**
 * Created by vas on 1/5/18.
 */

public class SchoolCursorWrapper extends CursorWrapper {

    public SchoolCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public School getSchool() {
        int id = getInt(getColumnIndex(SchoolTable.Cols.NEO_ID));
        String name = getString(getColumnIndex(SchoolTable.Cols.NAME));
        School school = new School();
        school.setId((long) id);
        school.setName(name);
        return school;
    }
}

