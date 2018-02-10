package ru.crew.motley.piideo.handshake.fragment

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_requets_received.view.*
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.activity.ChatActivity
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.fcm.FcmMessage
import ru.crew.motley.piideo.fcm.MessagingService
import ru.crew.motley.piideo.fcm.Receiver
import ru.crew.motley.piideo.util.TimeUtils

/**
 * Created by vas on 1/20/18.
 */
class RequestReceivedFragment : Fragment() {

    companion object {
        private const val ARG_DB_MESSAGE_ID = "local_db_id"
        fun newInstance(dbMessageId: String) = RequestReceivedFragment().apply {
            arguments = Bundle().apply { putString(ARG_DB_MESSAGE_ID, dbMessageId) }
        }
    }

    val messageId: String by lazy { arguments!!.getString(ARG_DB_MESSAGE_ID) }
    val message: FcmMessage by lazy { ChatLab.get(activity).getReducedFcmMessage(messageId) }
    val ownerId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    val database by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_requets_received, container, false)
        v.requester.text = "Friend"
        v.requestSubject.text = "[Stub Subject]"
        v.me.text = "[Me Name]"
        v.applyRequest.setOnClickListener {
            cancelAlarm()
            clearPreviousChat()
            sendOwnStubMessage()
//            sendAcknowledge()
            val i = ChatActivity.getIntent(messageId, activity)
            startActivity(i)
            activity?.finish()
        }
        v.rejectRequest.setOnClickListener {
            cancelAlarm()
            sendReject()
            activity?.finish()
        }
        return v
    }

    fun clearPreviousChat() {
        database.child("messages")
                .child(ownerId)
                .child(message.from)
                .removeValue()
        database.child("messages")
                .child(message.from)
                .child(ownerId)
                .removeValue()
    }

    fun sendReject() {
        val receiverId = message.from as String
        val message = createFcmMessage(ownerId, receiverId, "", MessagingService.REJ)
        database.child("notifications")
                .child("handshake")
                .push()
                .setValue(message)
    }

    fun sendOwnStubMessage() {
        val content = resources.getString(R.string.chat_message_stub)
        val message = createFcmMessage(message.from!!, ownerId, content, MessagingService.MSG)
        database.child("messages")
                .child(ownerId)
                .child(message.from)
                .push()
                .setValue(message)
    }

    fun cancelAlarm() {
        val alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(activity, Receiver::class.java)
        intent.action = "action"
        val pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, 0)
        alarmManager.cancel(pendingIntent)
    }

    fun createFcmMessage(from: String, to: String, content: String, type: String): FcmMessage {
        val now = System.currentTimeMillis()
        return FcmMessage(
                now,
                -now,
                TimeUtils.gmtDayTimestamp(now),
                from,
                to,
                content,
                type
        )
    }
}