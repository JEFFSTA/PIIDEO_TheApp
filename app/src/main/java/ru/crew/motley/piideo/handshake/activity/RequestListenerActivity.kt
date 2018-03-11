package ru.crew.motley.piideo.handshake.activity

import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.activity.ChatActivity
import ru.crew.motley.piideo.contacts.PhoneContactObserver
import ru.crew.motley.piideo.fcm.ShowDialogReceiver
import ru.crew.motley.piideo.network.activity.ConnectionErrorActivity

/**
 * Created by vas on 1/20/18.
 */
abstract class RequestListenerActivity : ConnectionErrorActivity() {

    val fbNotificationReceiver = ShowDialogReceiver(this)
    val contactsListener by lazy { PhoneContactObserver(Handler()) }

    override fun onResume() {
        super.onResume()
        (application as Appp).searchActivityResumed()
//        fbNotificationReceiver = ShowDialogReceiver(this)
        val filter = IntentFilter(ShowDialogReceiver.BROADCAST_ACTION)
        registerReceiver(fbNotificationReceiver, filter)
        contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                false,
                contactsListener)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(fbNotificationReceiver)
        (application as Appp).searchActivityPaused()
        contentResolver.unregisterContentObserver(contactsListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
    }

    fun showChat(dbMessageId: String, type: String) {
        val i = ChatActivity.getIntent(dbMessageId, this)
        startActivity(i)
        finish()
    }
}