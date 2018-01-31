package ru.crew.motley.piideo.contacts

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log

/**
 * Created by vas on 1/20/18.
 */
class PhoneContactObserver(handler: Handler): ContentObserver(handler) {
    override fun onChange(selfChange: Boolean) {
        this.onChange(selfChange, null)
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        Log.d(javaClass.canonicalName, "" + uri.toString())
    }
}