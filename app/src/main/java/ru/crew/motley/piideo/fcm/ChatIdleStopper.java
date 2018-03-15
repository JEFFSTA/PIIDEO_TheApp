package ru.crew.motley.piideo.fcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.crew.motley.piideo.SharedPrefs;

/**
 * Created by vas on 3/14/18.
 */

public class ChatIdleStopper extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPrefs.saveChatIdleStartTime(-1, context);
        SharedPrefs.clearChatData(context);
    }
}
