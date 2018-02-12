package ru.crew.motley.piideo.fcm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.support.annotation.StringDef
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.R
import ru.crew.motley.piideo.SharedPrefs
import ru.crew.motley.piideo.chat.activity.ChatActivity
import ru.crew.motley.piideo.chat.db.ChatLab
import ru.crew.motley.piideo.chat.fragment.ChatFragment
import ru.crew.motley.piideo.chat.model.PiideoLoader
import ru.crew.motley.piideo.fcm.MessagingService.Companion.SYN_ID
import ru.crew.motley.piideo.handshake.activity.HandshakeActivity
import ru.crew.motley.piideo.search.SearchRepeaterSingleton
import ru.crew.motley.piideo.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MessagingService : FirebaseMessagingService() {

    companion object {
        val TAG = MessagingService::class.java.simpleName

        var dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.getDefault())
                .apply { timeZone = TimeZone.getDefault() }

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

        const val SYN_REQUEST_CODE = 100
        const val ACK_REQUEST_CODE = 101
        const val MSG_REQUEST_CODE = 102
        const val PDO_REQUEST_CODE = 103

        const val SYN_ID = 200
        const val ACK_ID = 201
        const val REJ_ID = 202
        const val MSG_ID = 203
        const val PDO_ID = 204
    }

    @Inject
    lateinit var piideoLoader: PiideoLoader

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "Message received" + message.data)

//        if ((application as Appp).searchActivityVisible) {
        Log.d(TAG, "" + message.data.keys)
        Log.d(TAG, "" + message.data["senderUid"])
        Log.d(TAG, "" + message.data["receiverUid"])
        Log.d(TAG, "" + message.data["type"])
        Log.d(TAG, "" + message.data["content"])
        Log.d(TAG, "" + message.data["timestamp"])
        val messageId = saveMessageToDB(
                message.data["senderUid"]!!,
                message.data["receiverUid"]!!,
                message.data["content"] ?: "",
                message.data["type"]!!)
        val dbMessageId = messageId.toString()
        val calendar = if (message.data["timestamp"] != null) {
            Calendar.getInstance(TimeZone.getDefault()).apply {
                timeInMillis = message.data["timestamp"]!!.toLong()
            }
        } else {
            null
        }
        when (message.data["type"]) {
            SYN -> showRequestNotification(dbMessageId, message.data["type"]!!, calendar!!.time)
            ACK -> showChatOrNotification(dbMessageId, message.data["type"]!!, calendar!!.time)
            REJ -> sendNewRequest()
            MSG -> showNothingOrNotification(dbMessageId)
            PDO -> showPiideoNotyOrSkip(dbMessageId)
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

    private fun showChatOrNotification(dbMessageId: String, @MessageType type: String, timestamp: Date) {
        val app = application as Appp
        if (app.searchActivityVisible()) {
            showChat(dbMessageId)
        } else {
            showAcknowledgeNotification(dbMessageId, timestamp)
        }
    }

    private fun showChat(dbMessageId: String) {
        val i = ShowDialogReceiver.getIntent(dbMessageId, SYN)
        sendBroadcast(i)
    }

    private fun showRequestNotification(dbMessageId: String, @MessageType type: String, timestamp: Date) {
        clearChatTimeout()
//        clearHandShakeTimeout()
        if (chatIsActive()) return
        if (handShakeIsActive()) return
        val i = HandshakeActivity.getIntent(dbMessageId, type, applicationContext)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pI = notificationIntent(SYN_REQUEST_CODE, i)
        createChannelIfNeeded()
        showNotification(SYN_ID, pI, "", dateFormatter.format(timestamp) + " New request")
        SharedPrefs.saveHandshakeStartTime(Date().time, applicationContext)
        setAlarm(dbMessageId)
    }

    private fun showAcknowledgeNotification(dbMessageId: String, timestamp: Date) {
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pI = notificationIntent(ACK_REQUEST_CODE, i)
        showNotification(ACK_ID, pI, "", dateFormatter.format(timestamp) + " Approve")
    }

//    private fun showRejectNotification(dbMessageId: String, timestamp: Date) {
//        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
//        searchRepeater.next()
//        createChannelIfNeeded()
//        showNotification(REJ_ID, null, "", dateFormatter.format(timestamp) + " Reject")
//    }

    private fun sendNewRequest() {
        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
        searchRepeater.next()
    }

    private fun showNothingOrNotification(messageId: String) {
        val app = application as Appp
        if (!app.isChatActivityVisible and chatIsActive()) {
            showChatNotification(messageId)
        }
    }

    private fun showChatNotification(dbMessageId: String) {
        val lab = ChatLab.get(applicationContext)
        val content = lab.getReducedFcmMessage(dbMessageId).content!!
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
        val pI = notificationIntent(MSG_REQUEST_CODE, i)
        createChannelIfNeeded()
        showNotification(MSG_ID, pI, "", content)
    }

    private fun showPiideoNotyOrSkip(dbMessageId: String) {
        val app = application as Appp
        if (!app.isChatActivityVisible && chatIsActive()) {
            showPiideoNotification(dbMessageId)
            loadPiideo(dbMessageId)
        }
    }

    private fun chatIsActive() = SharedPrefs.loadChatMessageId(applicationContext) != null

    private fun handShakeIsActive() =
            SharedPrefs.loadChatStartTime(applicationContext) + TimeUnit.SECONDS.toMillis(HandshakeActivity.HANDSHAKE_TIMEOUT) > Date().time

    private fun showPiideoNotification(dbMessageId: String) {
        val lab = ChatLab.get(applicationContext)
        val content = lab.getReducedFcmMessage(dbMessageId).content!!
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
        val pI = notificationIntent(PDO_REQUEST_CODE, i)
        createChannelIfNeeded()
        showNotification(PDO_ID, pI, "", content)
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

    private fun clearChatTimeout() {
        val chatStartTime = SharedPrefs.loadChatStartTime(applicationContext)
        if (chatStartTime + TimeUnit.SECONDS.toMillis(ChatFragment.CHAT_TIMEOUT) < Date().time) {
            SharedPrefs.clearChatData(applicationContext)
        }
    }

    private fun clearHandShakeTimeout() {
        val handshakeStartTime = SharedPrefs.loadHandshakeStartTime(applicationContext)
        if (handshakeStartTime + TimeUnit.SECONDS.toMillis(HandshakeActivity.HANDSHAKE_TIMEOUT) < Date().time) {
            SharedPrefs.clearHandshakeStartTime(applicationContext)
        }
    }

    private fun notificationIntent(requestCode: Int, intent: Intent): PendingIntent =
            PendingIntent.getActivity(
                    applicationContext,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)


    private fun showNotification(id: Int, intent: PendingIntent?, title: String = "", content: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(applicationContext.getString(R.string.app_name))
                .setContentText(content)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()
        manager.notify(id, notification)
    }

    fun setAlarm(dbMessageId: String) {
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, Receiver::class.java)
        intent.action = "action"
        intent.putExtra("DB_MESSAGE_ID", dbMessageId)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        val executeAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(HandshakeActivity.HANDSHAKE_TIMEOUT)

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
    }
}

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(SYN_ID)
        val firstMessageId = intent.getStringExtra("DB_MESSAGE_ID")
        if (SharedPrefs.loadChatMessageId(context) == null) {
            val dbMessage = ChatLab.get(context).getReducedFcmMessage(firstMessageId)!!
            val timestamp = System.currentTimeMillis()
            val dayTimestamp = TimeUtils.gmtDayTimestamp(timestamp)
            val message = FcmMessage(
                    timestamp,
                    -timestamp,
                    dayTimestamp,
                    dbMessage.to,
                    dbMessage.from,
                    "",
                    MessagingService.REJ,
                    dbMessage.to + "_" + dbMessage.from)
            FirebaseDatabase.getInstance()
                    .reference
                    .child("notifications")
                    .child("handshake")
                    .push()
                    .setValue(message)
        }
    }
}