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
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.activity.ChatActivity
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.fcm.FcmMessage
import ru.crew.motley.piideo.fcm.MessagingService
import ru.crew.motley.piideo.fcm.Receiver
import ru.crew.motley.piideo.util.Utils
import android.provider.ContactsContract
import android.provider.BaseColumns
import android.net.Uri
import android.os.Build
import kotlinx.android.synthetic.main.fragment_request_received_0.view.*
import ru.crew.motley.piideo.SharedPrefs
import ru.crew.motley.piideo.fcm.AcknowledgeService.CHAT_IDLE_TIMEOUT
import ru.crew.motley.piideo.fcm.AcknowledgeService.REQUEST_CODE_IDLE_STOPPER
import ru.crew.motley.piideo.network.NetworkErrorCallback
import ru.crew.motley.piideo.registration.fragments.PhoneFragment.FRENCH_PREFIX
import ru.crew.motley.piideo.search.Events
import java.util.concurrent.TimeUnit


/**
 * Created by vas on 1/20/18.
 */
class RequestReceivedFragment : Fragment() {

    companion object {
        private const val ARG_DB_MESSAGE_ID = "local_db_id"
        fun newInstance(dbMessageId: String, errorCallback: NetworkErrorCallback) = RequestReceivedFragment().apply {
            arguments = Bundle().apply { putString(ARG_DB_MESSAGE_ID, dbMessageId) }
            this.errorCallback = errorCallback
        }
    }

    lateinit var errorCallback: NetworkErrorCallback
    val messageId: String by lazy { arguments!!.getString(ARG_DB_MESSAGE_ID) }
    val message: FcmMessage by lazy { ChatLab.get(activity).getReducedFcmMessage(messageId) }
    val ownerId by lazy { FirebaseAuth.getInstance().currentUser!!.uid }
    val database by lazy { FirebaseDatabase.getInstance().reference }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val friendNumber = message.content!!
        val v = if (friendNumber.startsWith("++"))
            inflater.inflate(R.layout.fragment_request_received_1, container, false)
        else
            inflater.inflate(R.layout.fragment_request_received_0, container, false)
        v.applyRequest.setOnClickListener {
            cancelAlarm()
            clearPreviousChat()
            sendOwnStubMessage()
            SharedPrefs.saveChatIdleStartTime(System.currentTimeMillis(), context)
            initChatIdle()
            val i = ChatActivity.getIntent(messageId, activity)
            startActivity(i)
            SharedPrefs.clearHandshakeStartTime(context)
            activity?.finish()
        }
        v.rejectRequest.setOnClickListener {

            cancelAlarm()
            sendReject()
            SharedPrefs.clearHandshakeStartTime(context)
            activity?.finish()
        }
        if (friendNumber.startsWith("++")) {
            setMeFriendText(v, friendNumber.substring(2).split("||")[0])
            v.subject.text = friendNumber.substring(2).split("||")[1].split("|")[0]
            v.explanation.text = friendNumber.substring(2).split("||")[1].split("|")[1]
        } else {
            setMeFriendText(v, message.content!!.split("||")[0])
            v.subject.text = message.content!!.split("||")[1].split("|")[0]
            v.explanation.text = message.content!!.split("||")[1].split("|")[1]
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
                .addOnFailureListener { errorCallback.onError() }
    }

    fun sendOwnStubMessage() {
        val content = resources.getString(R.string.chat_message_stub)
        val message = createFcmMessage(message.from!!, ownerId, content, MessagingService.MSG)
        database.child("messages")
                .child(ownerId)
                .child(message.from)
                .push()
                .setValue(message)
                .addOnFailureListener { errorCallback.onError() }
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
                Utils.gmtDayTimestamp(now),
                from,
                to,
                content,
                type
        )
    }

    private fun setMeFriendText(v: View, friendNumber: String) {
        var contactName = findContactName(friendNumber)
        if (contactName.isBlank()) {
            contactName = findContactName(FRENCH_PREFIX + friendNumber)
        }
        v.meFriend.text = contactName
    }

    private fun findContactName(friendNumber: String): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(friendNumber))
        String
        activity?.contentResolver
                ?.query(
                        uri,
                        arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME),
                        null,
                        null,
                        null)
                .use { contacts ->
                    if (contacts != null && contacts.count > 0) {
                        contacts.moveToNext()
                        val index = contacts.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)
                        return contacts.getString(index)
                    }
                    return ""
                }
    }

    private fun initChatIdle() {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent()
        intent.action = Events.BROADCAST_CHAT_IDLE_STOP

        val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_IDLE_STOPPER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val executeAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(CHAT_IDLE_TIMEOUT.toLong())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent)
        }
        SharedPrefs.saveChatIdleStartTime(System.currentTimeMillis(), context)
    }
}