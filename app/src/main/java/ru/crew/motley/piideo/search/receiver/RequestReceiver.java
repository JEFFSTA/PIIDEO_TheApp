package ru.crew.motley.piideo.search.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ru.crew.motley.piideo.search.service.RequestService;

/**
 * Created by vas on 3/3/18.
 */

public class RequestReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent onPush = RequestService.getIntent(context);
        context.startService(onPush);
    }
}
