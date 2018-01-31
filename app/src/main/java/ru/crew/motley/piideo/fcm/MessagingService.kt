package ru.crew.motley.piideo.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.support.annotation.StringDef
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.chat.activity.ChatActivity
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.chat.model.PiideoLoader
import ru.crew.motley.piideo.handshake.activity.HandshakeActivity
import ru.crew.motley.piideo.search.SearchRepeaterSingleton
import javax.inject.Inject


/**
 * Created by vas on 1/11/18.
 */

class MessagingService : FirebaseMessagingService() {

    companion object {
        val TAG = MessagingService::class.java.simpleName

        @StringDef(SYN, ACK, ACC, REJ, MSG, PDO)
        @Retention(AnnotationRetention.SOURCE)
        annotation class MessageType

        const val SYN = "synchronize"
        const val ACK = "acknowledge"
        const val ACC = "accept"
        const val REJ = "reject"
        const val MSG = "message"
        const val PDO = "piideo"

        const val NOTIFICATION_CHANNEL_DEFAULT = "default"
    }

    @Inject
    lateinit var piideoLoader: PiideoLoader

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Message received" + message.data)

//        if ((application as Appp).isActivityVisible) {
        Log.d(TAG, "" + message.data.keys)
        Log.d(TAG, "" + message.data["senderUid"])
        Log.d(TAG, "" + message.data["receiverUid"])
        Log.d(TAG, "" + message.data["type"])
        Log.d(TAG, "" + message.data["content"])
        val messageId = saveMessageToDB(
                message.data["senderUid"]!!,
                message.data["receiverUid"]!!,
                message.data["content"] ?: "",
                message.data["type"]!!)
        when (message.data["type"]) {
            SYN -> showRequestNotification(messageId.toString(), message.data["type"]!!)
//            ACK -> showAcknowledgeNotification(messageId)
            ACK -> showChatOrNotification(messageId.toString(), message.data["type"]!!)
            REJ -> showRejectNotification(messageId.toString())
            MSG -> showNothingOrNotification(messageId.toString())
            PDO -> showPiideoNotyOrSkip(messageId.toString())
            else -> {
                Log.e(TAG, "Message type error")
                throw RuntimeException("Fcm meesage type is unsupported")
            }
        }
    }

    private fun saveMessageToDB(from: String, to: String, content: String, @MessageType type: String): Long {
        val lab = ChatLab.get(applicationContext)
        val fcmMessage = FcmMessage(from = from, to = to, content = content, type = type)
        return lab.addMessage(fcmMessage)
    }

    private fun showChatOrNotification(dbMessageId: String, @MessageType type: String) {
        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
        searchRepeater.stopSearch()
        val app = application as Appp
        if (app.isActivityVisible) {
            showChat(dbMessageId)
        } else {
            showAcknowledgeNotification(dbMessageId)
        }
    }

    private fun showChat(dbMessageId: String) {
        val i = ShowDialogReceiver.getIntent(dbMessageId, SYN)
        sendBroadcast(i)
    }

    private fun showRequestNotification(dbMessageId: String, @MessageType type: String) {
        val i = HandshakeActivity.getIntent(dbMessageId, type, applicationContext)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pI = PendingIntent.getActivity(
                applicationContext,
                100,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded()
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("New request has been received")
                .setStyle(NotificationCompat.BigTextStyle().bigText("[Request]"))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pI)
                .setAutoCancel(true)
                .build()
        manager.notify(101, notification)
        println("alarm send notif")
    }

    private fun showAcknowledgeNotification(dbMessageId: String) {
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
        val pI = PendingIntent.getActivity(
                applicationContext,
                102,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("Approve")
                .setStyle(NotificationCompat.BigTextStyle().bigText("[Approve]"))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pI)
                .setAutoCancel(true)
                .build()
        manager.notify(103, notification)
    }

    private fun showRejectNotification(dbMessageId: String) {
        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
        searchRepeater.stopSearch()
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText("Reject")
                .setStyle(NotificationCompat.BigTextStyle().bigText("[Reject]"))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                .setContentIntent(pI)
                .setAutoCancel(true)
                .build()
        manager.notify(105, notification)
    }

    private fun showNothingOrNotification(messageId: String) {
        val app = application as Appp
        if (!app.isChatVisible) {
            showChatNotification(messageId)
        }
    }

    private fun showChatNotification(dbMessageId: String) {
        val lab = ChatLab.get(applicationContext)
        val content = lab.getReducedFcmMessage(dbMessageId).content
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
        val pI = PendingIntent.getActivity(
                applicationContext,
                106,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText("New Message"))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pI)
                .setAutoCancel(true)
                .build()
        manager.notify(107, notification)
    }

    private fun showPiideoNotyOrSkip(dbMessageId: String) {
        val app = application as Appp
        if (!app.isChatVisible) {
            showPiideoNotification(dbMessageId)
            loadPiideo(dbMessageId)
        }
    }

    private fun showPiideoNotification(dbMessageId: String) {
        val lab = ChatLab.get(applicationContext)
        val content = lab.getReducedFcmMessage(dbMessageId).content
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
        val pI = PendingIntent.getActivity(
                applicationContext,
                108,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText("New Piideo"))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pI)
                .setAutoCancel(true)
                .build()
        manager.notify(109, notification)
    }

    private fun loadPiideo(dbMessageId: String) {
        val lab = ChatLab.get(applicationContext)
        val message = lab.getReducedFcmMessage(dbMessageId)
        piideoLoader.receive(message.content!!, message.from!!, message.to!!)
                .subscribe()
    }


    private fun createChannelIfNeeded() {
        val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_DEFAULT)
            if (channel != null) {
                return
            }
            val defaultChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_DEFAULT,
                    "Request Channel",
                    NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(defaultChannel)
        }
    }
}