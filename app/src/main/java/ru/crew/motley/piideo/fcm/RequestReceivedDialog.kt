package ru.crew.motley.piideo.fcm

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_request_received.view.*
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.db.ChatLab
import java.util.*


fun Date.dayTimeStamp(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.time
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MINUTE, 0)
    return calendar.timeInMillis
}

class RequestReceivedDialog : DialogFragment() {
    private var reference: DatabaseReference? = null

//    private var ok: Button? = null
//    private var pb: ProgressBar? = null
//    private var code: EditText? = null

    private lateinit var receivedMessageId: String
    private lateinit var messageType: String
    private lateinit var message: FcmMessage
    private lateinit var ownerId: String

    val mDatabase = FirebaseDatabase.getInstance().reference

    companion object {
        private val ARG_MESSAGE_ID = "message_id"
        private val ARG_DIALOG_TYPE = "dialog_type"
        fun getInstance(receivedMessageId: String, messageType: String): RequestReceivedDialog {
            val args = Bundle()
            args.putString(ARG_MESSAGE_ID, receivedMessageId)
            args.putString(ARG_DIALOG_TYPE, messageType)
            val dialog = RequestReceivedDialog()
            dialog.arguments = args
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receivedMessageId = arguments!!.getString(ARG_MESSAGE_ID)!!
        messageType = arguments!!.getString(ARG_DIALOG_TYPE)!!
        val lab = ChatLab.get(activity)
        message = lab.getReducedFcmMessage(receivedMessageId)
        ownerId = FirebaseAuth.getInstance().currentUser!!.uid
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = LayoutInflater.from(activity).inflate(R.layout.dialog_request_received, null)
        val builder = AlertDialog.Builder(activity!!).setView(v)
        when (messageType) {
            "synchronize" -> {
                v.receivedMessage.visibility = View.GONE
                builder.setPositiveButton("Apply") { _, _ ->
                    sendAcknowledge(v)
                    dismiss()
                }.setNegativeButton("Reject") { _, _ ->
                    sendReject(v)
                    dismiss()
                }
            }
            "acknowledge" -> {
                v.receivedMessage.visibility = View.VISIBLE
                v.receivedMessage.text = message.content
                builder.setPositiveButton("Accept") { _, _ ->
                    sendAccept(v)
                    dismiss()
                }
            }
            "accept" -> {
                v.testMessage.visibility = View.GONE
                v.receivedMessage.visibility = View.VISIBLE
                v.receivedMessage.text = message.content
                builder.setPositiveButton("End") { _, _ ->
                    dismiss()
                }
            }
            "reject" -> {
                v.testMessage.visibility = View.GONE
                v.receivedMessage.visibility = View.VISIBLE
                v.receivedMessage.text = "Request rejected"
                builder.setPositiveButton("End") { _, _ ->
                    dismiss()
                }
            }
        }
        return builder.create()
    }

    fun sendAcknowledge(v: View) {
        val now = Date()

        val testString = v.testMessage?.text.toString()
        val receiverId = message.from

        val message = FcmMessage(now.time, -now.time, now.dayTimeStamp(), ownerId, receiverId, testString, "acknowledge")

        mDatabase
                .child("notifications")
                .child("handshake")
                .push()
                .setValue(message)
        mDatabase
                .child("messages")
                .child(receiverId)
                .child(ownerId)
                .push()
                .setValue(message)
        if (receiverId != ownerId) {
            mDatabase
                    .child("messages")
                    .child(ownerId)
                    .child(receiverId)
                    .push()
                    .setValue(message)
        }
    }

    fun sendAccept(v: View) {
        val now = Date()
        val testString = v.testMessage?.text.toString()
        val receiverId = message.from

        val message = FcmMessage(now.time, -now.time, now.dayTimeStamp(), ownerId, receiverId, testString, type = "accept")

        mDatabase
                .child("notifications")
                .child("handshake")
                .push()
                .setValue(message)
        mDatabase
                .child("messages")
                .child(receiverId)
                .child(ownerId)
                .push()
                .setValue(message)
        if (receiverId != ownerId) {
            mDatabase
                    .child("messages")
                    .child(ownerId)
                    .child(receiverId)
                    .push()
                    .setValue(message)
        }
    }

    fun sendReject(v: View) {
        val now = Date()

        val receiverId = message.from

        val message = FcmMessage(now.time, -now.time, now.dayTimeStamp(), ownerId, receiverId, "", type = "reject")

        mDatabase
                .child("notifications")
                .child("handshake")
                .push()
                .setValue(message)
        mDatabase
                .child("messages")
                .child(receiverId)
                .child(ownerId)
                .push()
                .setValue(message)
        if (receiverId != ownerId) {
            mDatabase
                    .child("messages")
                    .child(ownerId)
                    .child(receiverId)
                    .push()
                    .setValue(message)
        }
    }
}