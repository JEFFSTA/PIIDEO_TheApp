package ru.crew.motley.piideo.handshake.fragment

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
import ru.crew.motley.piideo.fcm.dayTimeStamp
import java.util.*

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
            sendAcknowledge()
            val i = ChatActivity.getIntent(messageId, activity)
            startActivity(i)
            activity?.finish()
        }
        v.rejectRequest.setOnClickListener {
            sendReject()
            activity?.finish()
        }
        return v
    }

    fun sendAcknowledge() {
        val now = Date()
        val receiverId = message.from
        val message = FcmMessage(
                now.time,
                -now.time,
                now.dayTimeStamp(),
                ownerId,
                receiverId,
                content = "",
                type = "acknowledge")
        database.child("notifications")
                .child("handshake")
                .push()
                .setValue(message)
    }

    fun sendReject() {
        val now = Date()
        val receiverId = message.from
        val message = FcmMessage(
                now.time,
                -now.time,
                now.dayTimeStamp(),
                ownerId,
                receiverId,
                content = "",
                type = "reject")
        database.child("notifications")
                .child("handshake")
                .push()
                .setValue(message)
    }

}