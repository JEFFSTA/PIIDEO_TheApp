package ru.crew.motley.piideo.search.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.crew.motley.piideo.search.service.PushBroadcastHandler;

/**
 * Created by vas on 3/1/18.
 */

public class PushBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent onPush = new Intent(context, PushBroadcastHandler.class);
        context.startService(onPush);
    }
}
