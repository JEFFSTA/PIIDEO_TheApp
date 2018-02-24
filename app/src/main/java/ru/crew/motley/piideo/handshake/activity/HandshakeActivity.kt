package ru.crew.motley.piideo.handshake.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.SharedPrefs
import ru.crew.motley.piideo.fcm.MessagingService.Companion.MessageType
import ru.crew.motley.piideo.handshake.NavigationCallback
import ru.crew.motley.piideo.handshake.fragment.HandshakeTimeoutFragment
import ru.crew.motley.piideo.handshake.fragment.RequestReceivedFragment
import ru.crew.motley.piideo.splash.SplashActivity
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by vas on 1/20/18.
 */

class HandshakeActivity : AppCompatActivity(), NavigationCallback {

    companion object {

        private const val EXTRA_DB_MESSAGE_ID = "local_db_id"
        private const val EXTRA_TYPE = "message_type"

        fun getIntent(dbMessageId: String, @MessageType type: String, context: Context) =
                Intent(context, HandshakeActivity::class.java).apply {
                    putExtra(EXTRA_DB_MESSAGE_ID, dbMessageId)
                    putExtra(EXTRA_TYPE, type)
                }

        const val HANDSHAKE_TIMEOUT = 45L
    }

    val handler = Handler()
    val timer = Timer()

    val messageId by lazy { intent.getStringExtra(EXTRA_DB_MESSAGE_ID)!! }
    val type by lazy { intent.getStringExtra(EXTRA_TYPE)!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        showResponseFragment()
    }

    fun showResponseFragment() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, RequestReceivedFragment.newInstance(messageId))
                .commit()
    }

    override fun onResume() {
        super.onResume()
        val startTime = SharedPrefs.loadHandshakeStartTime(this)
        val endTime = startTime + TimeUnit.SECONDS.toMillis(HANDSHAKE_TIMEOUT)
        if (endTime < Date().time) {
            SharedPrefs.clearHandshakeStartTime(this)
            showTimeoutMessage()
        } else {
            handler.postDelayed(timer, endTime - Date().time)
        }
        (application as Appp).handshakeActivityResumed()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timer)
        (application as Appp).handshaekActivityPaused()
    }

    override fun end() {
        val i = SplashActivity.getIntent(this)
        startActivity(i)
        finish()
    }

    fun showTimeoutMessage() {
        val messageFragment = HandshakeTimeoutFragment.newInstance(this)
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, messageFragment)
                .commit()
    }

    inner class Timer : Runnable {
        override fun run() {
            SharedPrefs.clearHandshakeStartTime(applicationContext)
            this@HandshakeActivity.finish()
        }
    }
}
