package ru.crew.motley.piideo.handshake.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.fcm.MessagingService.Companion.MessageType
import ru.crew.motley.piideo.handshake.fragment.RequestReceivedFragment

/**
 * Created by vas on 1/20/18.
 */

class HandshakeActivity : AppCompatActivity() {

    companion object {

        private val EXTRA_DB_MESSAGE_ID = "local_db_id"
        private val EXTRA_TYPE = "message_type"

        fun getIntent(dbMessageId: String, @MessageType type: String, context: Context) =
                Intent(context, HandshakeActivity::class.java).apply {
                    putExtra(EXTRA_DB_MESSAGE_ID, dbMessageId)
                    putExtra(EXTRA_TYPE, type)
                }
    }

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
}