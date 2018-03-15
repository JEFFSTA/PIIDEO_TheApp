package ru.crew.motley.piideo.fcm

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.search.activity.SearchActivity
import java.lang.ref.WeakReference
import ru.crew.motley.piideo.fcm.MessagingService.Companion.MessageType
import ru.crew.motley.piideo.fcm.MessagingService.Companion.TAG
import java.util.*

/**
 * Created by vas on 1/14/18.
 */
class ShowDialogReceiver(activity: AppCompatActivity) : BroadcastReceiver() {

    private var weakActivity: WeakReference<Activity> = WeakReference(activity)

    companion object {
        val BROADCAST_ACTION = "broadcast_for_dialog"
        val EXTRA_ID = "db_message_id"

        fun getIntent(dbMessageId: String) = Intent(BROADCAST_ACTION).apply {
            putExtra(EXTRA_ID, dbMessageId)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        val dbMessageId = intent.getStringExtra(EXTRA_ID)!!
        val date = Date()
        Log.d(TAG, "on Receive show chat " + date.time)
        Log.d(TAG, "weakActivity.get " + (weakActivity.get() == null) + " " + date.time)
        weakActivity.get()?.let {
            val app = it.application as Appp
            if (app.searchActivityVisible()) {
                (it as SearchActivity).showChat(dbMessageId)
                it.finish()
            }
        }
    }
}