package ru.crew.motley.piideo.chat.db

import android.database.Cursor
import android.database.CursorWrapper
import ru.crew.motley.piideo.fcm.FcmMessage

class FcmMessageCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {

    fun getFcmMessage() = FcmMessage(
            content = getString(getColumnIndex(PiideoSchema.MessageTable.Cols.CONTENT)),
            from = getString(getColumnIndex(PiideoSchema.MessageTable.Cols.SENDER_ID)),
            to = getString(getColumnIndex(PiideoSchema.MessageTable.Cols.RECEIVER_ID)),
            type = getString(getColumnIndex(PiideoSchema.MessageTable.Cols.MSG_TYPE))
    )
}