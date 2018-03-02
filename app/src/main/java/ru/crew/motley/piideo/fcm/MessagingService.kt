package ru.crew.motley.piideo.fcm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.annotation.StringDef
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.View
import android.widget.RemoteViews
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
        const val ACK_REQUEST_CODE = 109
        const val REJ_REQUEST_CODE = 104
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
            return
//            null
        }
        val logger = MessageLogger()
        val date = Date()
        logger.saveToLogFile("onMessageReceived", date.time)
        logger.saveToLogFile(message.data["type"]!!, date.time)
        when (message.data["type"]) {
            SYN -> showRequestNotification(dbMessageId, message.data["type"]!!, calendar!!.time, message.data["content"]!!)
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
        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
        searchRepeater.stop()
//        SharedPrefs.setSearching(false, applicationContext)
        val app = application as Appp
        val params = Bundle()
        val date = Date()

//        val visible  = if (app.searchActivityVisible()) 1L else 0L
//        params.putLong("searchActivityVisible", visible )
//        params.putLong("timeInMillis", date.time)
//        FirebaseAnalytics.getInstance(this).logEvent("showChatOrNotification", params)
//        mFirebaseAnalytics.logEvent("share_image", params);
        val logger = MessageLogger()
        logger.saveToLogFile("showChatOrNotification", date.time)
        logger.saveToLogFile("searchActivityVisible " + app.searchActivityVisible(), date.time)
        if (app.searchActivityVisible()) {
            showChat(dbMessageId, params)
        } else {
            showAcknowledgeNotification(dbMessageId, timestamp)
        }
    }

    private fun showChat(dbMessageId: String, logBundle: Bundle) {
        val i = ShowDialogReceiver.getIntent(dbMessageId, SYN)
//        logBundle.putString("sendBroadCast", "executing")
        val logger = MessageLogger()
        val date = Date()
        logger.saveToLogFile("showChat", date.time)
        sendBroadcast(i)
//        FirebaseAnalytics.getInstance(this).logEvent("broadCastToHandshake", logBundle)
    }

    private fun showRequestNotification(dbMessageId: String, @MessageType type: String, timestamp: Date, content: String) {
        clearChatTimeout()
//        clearHandShakeTimeout()
        if (chatIsActive()) return
        if (handShakeIsActive()) return
        val i = HandshakeActivity.getIntent(dbMessageId, type, applicationContext)
        i.action = System.currentTimeMillis().toString()
//        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pI = notificationIntent(SYN_REQUEST_CODE, i)
        createChannelIfNeeded()
        val title = resources.getString(R.string.nty_request)
//        val content = ownSubject()
        showCustomNotification(SYN_ID, pI, title, ownSubject())
        SharedPrefs.saveHandshakeStartTime(Date().time, applicationContext)
        val topicArr = content.split("||")
        val subject = if (topicArr.size == 1) {
            topicArr[0].split("|")[0]
        } else {
            topicArr[1].split("|")[0]
        }
        val explaination = if (topicArr.size == 1) {
            topicArr[0].split("|")[1]
        } else {
            topicArr[1].split("|")[1]
        }
        SharedPrefs.searchSubject(subject, applicationContext)
        SharedPrefs.requestMessage(explaination, applicationContext)
        setAlarm(dbMessageId)
    }

    private fun ownSubject(): String {
        val lab = ChatLab.get(applicationContext)
        return lab.member.subject.name
    }

    private fun showAcknowledgeNotification(dbMessageId: String, timestamp: Date) {
        val i = ChatActivity.getIntent(dbMessageId, applicationContext)
//        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pI = notificationIntent(ACK_REQUEST_CODE, i)
        val title = resources.getString(R.string.nty_accepted)
//        showNotification(ACK_ID, pI, title, "")
        showCustomNotification(ACK_ID, pI, title, "")
    }

//    private fun showRejectNotification(dbMessageId: String, timestamp: Date) {
//        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
//        searchRepeater.next()
//        createChannelIfNeeded()
//        showNotification(REJ_ID, null, "", dateFormatter.format(timestamp) + " Reject")
//    }

    private fun sendNewRequest() {
        val searchRepeater = SearchRepeaterSingleton.instance(applicationContext)
        searchRepeater.skip()
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
        val title = resources.getString(R.string.nty_message)
        showCustomNotification(MSG_ID, pI, title, content)
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
        val title = resources.getString(R.string.nty_piideo)
        showCustomNotification(PDO_ID, pI, title, "")
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
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)


    private fun showNotification(id: Int, intent: PendingIntent?, title: String = "", content: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val largeIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_new)
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setContentTitle(title)
                .setContentText(content)
//                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setLargeIcon(largeIcon)
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

        val pendingIntent = PendingIntent.getBroadcast(this, 100500, intent, 0)
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

    private fun showCustomNotification(id: Int, intent: PendingIntent?, title: String = "", content: String) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val contentView = createCustomNotification(title, content)
        val notification = NotificationCompat.Builder(
                applicationContext,
                NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContent(contentView)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build()
        manager.notify(id, notification)
    }

    private fun createCustomNotification(title: String, content: String = ""): RemoteViews {
        val contentView = RemoteViews(packageName, R.layout.custom_push)
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher_new)
        contentView.setTextViewText(R.id.title, title)
        var reducedContent = content.split("\n")[0]
        if (reducedContent.length > 15) {
            reducedContent = reducedContent.substringAfter(" ", reducedContent.substring(15))
        }
        contentView.setTextViewText(R.id.text, reducedContent)
        if (content.isBlank()) {
            contentView.setViewVisibility(R.id.text, View.GONE)
        } else {
            contentView.setViewVisibility(R.id.text, View.VISIBLE)

        }
        return contentView
    }

}

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(SYN_ID)
        val firstMessageId = intent.getStringExtra("DB_MESSAGE_ID")
//        if (SharedPrefs.loadChatMessageId(context) == null) {
//            val dbMessage = ChatLab.get(context).getReducedFcmMessage(firstMessageId)!!
//            val timestamp = System.currentTimeMillis()
//            val dayTimestamp = TimeUtils.gmtDayTimestamp(timestamp)
//            val message = FcmMessage(
//                    timestamp,
//                    -timestamp,
//                    dayTimestamp,
//                    dbMessage.to,
//                    dbMessage.from,
//                    "",
//                    MessagingService.REJ,
//                    dbMessage.to + "_" + dbMessage.from)
//            FirebaseDatabase.getInstance()
//                    .reference
//                    .child("notifications")
//                    .child("handshake")
//                    .push()
//                    .setValue(message)
//        }
    }
}