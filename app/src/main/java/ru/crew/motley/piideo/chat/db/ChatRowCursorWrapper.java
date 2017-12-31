package ru.crew.motley.piideo.chat.db;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * Created by vas on 12/26/17.
 */

public class ChatRowCursorWrapper extends CursorWrapper {

    public ChatRowCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public PiideoRow getPiideoRow() {
        int id = getInt(getColumnIndex(PiideoSchema.ChatTable.Cols.UUID));
        String piideoFile = getString(getColumnIndex(PiideoSchema.ChatTable.Cols.PIIDEO_FILE));
        PiideoRow row = new PiideoRow(id);
        row.setPiideoFileName(piideoFile);
        return row;
    }
}
