package ru.crew.motley.piideo.fcm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import ru.crew.motley.piideo.Appp;
import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.SharedPrefs;
import ru.crew.motley.piideo.chat.activity.ChatActivity;
import ru.crew.motley.piideo.chat.db.ChatLab;
import ru.crew.motley.piideo.search.Events;
import ru.crew.motley.piideo.search.service.RequestService;

import static ru.crew.motley.piideo.fcm.MessagingService.ACK;
import static ru.crew.motley.piideo.fcm.MessagingService.ACK_ID;
import static ru.crew.motley.piideo.fcm.MessagingService.ACK_REQUEST_CODE;

/**
 * Created by vas on 3/14/18.
 */

public class AcknowledgeService extends AbstractMService {

    private static final String TAG = AcknowledgeService.class.getSimpleName();

    private static final String EXTRA_MESSAGE_ID = "message_id";

    public static final int CHAT_IDLE_TIMEOUT = 2 * 60;

    public static final int REQUEST_CODE_IDLE_STOPPER = 51;

    private String mDBmessageId;

    public static Intent getIntent(Context context, String dbMessageId) {
        Intent i = new Intent(context, AcknowledgeService.class);
        i.putExtra(EXTRA_MESSAGE_ID, dbMessageId);
        return i;
    }

    public AcknowledgeService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        RequestService.stopRestarting(getApplicationContext());
        SharedPrefs.setSearching(false, getApplicationContext());
        SharedPrefs.setSearchCount(-1, getApplicationContext());
        SharedPrefs.saveChatSide(ACK, getApplicationContext());
        initChatIdle();
        ChatLab.get(getApplicationContext()).clearQueue();
        mDBmessageId = intent.getStringExtra(EXTRA_MESSAGE_ID);
        Appp app = (Appp) getApplication();
        if (app.searchActivityVisible()) {
            showChat();
        } else {
            showAcknowledgeNotification();
        }
    }

    private void showChat() {
        Intent i = ShowDialogReceiver.Companion.getIntent(mDBmessageId);
        sendBroadcast(i);
    }

    private void showAcknowledgeNotification() {
        Intent i = ChatActivity.getIntent(mDBmessageId, getApplicationContext());
        PendingIntent pI = notificationIntent(ACK_REQUEST_CODE, i);
        String title = getResources().getString(R.string.nty_accepted);
        showCustomNotification(ACK_ID, pI, title, "");
    }

    private void initChatIdle() {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(Events.BROADCAST_CHAT_IDLE_STOP);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                REQUEST_CODE_IDLE_STOPPER,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        long executeAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(CHAT_IDLE_TIMEOUT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    executeAt,
                    pendingIntent);
        }
        SharedPrefs.saveChatIdleStartTime(System.currentTimeMillis(), getApplicationContext());
    }


}
