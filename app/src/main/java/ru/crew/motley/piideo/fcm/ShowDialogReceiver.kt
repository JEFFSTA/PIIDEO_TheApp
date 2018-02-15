package ru.crew.motley.piideo.fcm

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.search.activity.SearchActivity
import java.lang.ref.WeakReference
import ru.crew.motley.piideo.fcm.MessagingService.Companion.MessageType
import java.util.*

/**
 * Created by vas on 1/14/18.
 */
class ShowDialogReceiver(activity: AppCompatActivity) : BroadcastReceiver() {

    private var weakActivity: WeakReference<Activity> = WeakReference(activity)

    companion object {
        val BROADCAST_ACTION = "broadcast_for_dialog"

        val EXTRA_TYPE = "message_type"
        val EXTRA_ID = "db_message_id"

        fun getIntent(dbMessageId: String, @MessageType type: String) = Intent(BROADCAST_ACTION).apply {
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ID, dbMessageId)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)!!
        val dbMessageId = intent.getStringExtra(EXTRA_ID)!!

        val params = Bundle()
        val date = Date()
        params.putLong("timeInMillis", date.time)
        params.putBoolean("weakActivityIsNotNull", weakActivity.get() != null)
        FirebaseAnalytics.getInstance(context).logEvent("receiveByActivity", params)

        weakActivity.get()?.let {
            val app = it.application as Appp
            if (app.searchActivityVisible()) {
                (it as SearchActivity).showChat(dbMessageId, type)
                it.finish()
            }
        }
    }
}