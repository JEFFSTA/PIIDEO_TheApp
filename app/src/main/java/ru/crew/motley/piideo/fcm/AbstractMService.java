package ru.crew.motley.piideo.fcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import ru.crew.motley.piideo.R;

import static ru.crew.motley.piideo.fcm.MessagingService.NOTIFICATION_CHANNEL_DEFAULT;

/**
 * Created by vas on 3/14/18.
 */

public abstract class AbstractMService extends IntentService {

    public AbstractMService(String name) {
        super(name);
    }

    protected PendingIntent notificationIntent(int requestCode, Intent intent) {
        intent.setAction("" + System.currentTimeMillis());
        return PendingIntent.getActivity(
                getApplicationContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }

    protected void showCustomNotification(int id, PendingIntent intent, String title, String content) {
        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews contentView = createCustomNotification(title, content);
        Notification notification = new NotificationCompat.Builder(
                getApplicationContext(),
                NOTIFICATION_CHANNEL_DEFAULT)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContent(contentView)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .build();
        manager.notify(id, notification);
    }

    private RemoteViews createCustomNotification(String title, String content) {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_push);
        contentView.setImageViewResource(R.id.image, R.mipmap.ic_launcher_new);
        contentView.setTextViewText(R.id.title, title);
        String reducedContent = content.split("\n")[0];
        if (reducedContent.length() > 15) {
            reducedContent = reducedContent.substring(15);
        }
        contentView.setTextViewText(R.id.text, reducedContent);
        if (content.isEmpty()) {
            contentView.setViewVisibility(R.id.text, View.GONE);
        } else {
            contentView.setViewVisibility(R.id.text, View.VISIBLE);

        }
        return contentView;
    }

}
