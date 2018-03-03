package ru.crew.motley.piideo.search.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.parceler.Parcels;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.chat.activity.ChatActivity;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.network.Member;
import ru.crew.motley.piideo.search.activity.SearchActivity;

import static ru.crew.motley.piideo.fcm.MessagingService.MSG_REQUEST_CODE;
import static ru.crew.motley.piideo.fcm.MessagingService.NOTIFICATION_CHANNEL_DEFAULT;
import static ru.crew.motley.piideo.fcm.MessagingService.REJ_ID;

/**
 * Created by vas on 3/1/18.
 */

public class PushBroadcastHandler extends IntentService {

    private static String TAG = PushBroadcastHandler.class.getSimpleName();

    public PushBroadcastHandler() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Handle intent");
        ChatLab lab = ChatLab.get(getApplicationContext());
        Member member = lab.getMember();
        Parcelable memberParceled = Parcels.wrap(member);
        Intent i = SearchActivity.getIntent(memberParceled, getApplicationContext());
        i.setAction(Long.toString(System.currentTimeMillis()));
        PendingIntent pI = notificationIntent(MSG_REQUEST_CODE, i);
        createChannelIfNeeded();
        String title = getResources().getString(R.string.nty_rejected);
        showCustomNotification(REJ_ID, pI, title);
    }

    private void showCustomNotification(int id, PendingIntent intent, String title) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews contentView = createCustomNotification(title);
        Notification notification = new NotificationCompat.Builder(
                getApplicationContext(),
                NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContent(contentView)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();
        manager.notify(id, notification);
    }

    private RemoteViews createCustomNotification(String title) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_push);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher_new);
        contentView.setTextViewText(R.id.title, title);
        contentView.setViewVisibility(R.id.text, View.GONE);
        return contentView;
    }

    private void createChannelIfNeeded() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = manager.getNotificationChannel(NOTIFICATION_CHANNEL_DEFAULT);
            if (channel != null) {
                return;
            }
            NotificationChannel defaultChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_DEFAULT,
                    "Request Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(defaultChannel);
        }
    }

    private PendingIntent notificationIntent(int requestCode, Intent intent) {
        return PendingIntent.getActivity(
                getApplicationContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
