package ru.crew.motley.piideo.fcm

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import ru.crew.motley.piideo.Appp
import ru.crew.motley.piideo.search.activity.SearchActivity
import java.lang.ref.WeakReference
import ru.crew.motley.piideo.fcm.MessagingService.Companion.MessageType

/**
 * Created by vas on 1/14/18.
 */
class ShowDialogReceiver(activity: AppCompatActivity) : BroadcastReceiver() {

    private var weakActivity: WeakReference<Activity> = WeakReference(activity)

    companion object {
        val BROADCAST_ACTION = "broadcast_for_dialog"

        val EXTRA_TYPE = "message_type"
        val EXTRA_ID = "db_message_id"

        fun getIntent(dbMessageId: String, @MessageType type: String) = Intent(BROADCAST_ACTION).apply {
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ID, dbMessageId)
        }
    }


    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)!!
        val dbMessageId = intent.getStringExtra(EXTRA_ID)!!

        weakActivity.get()?.let {
            val app = it.application as Appp
            if (app.isActivityVisible) {
                (it as SearchActivity).showChat(dbMessageId, type)
            }
        }
    }

//    br = new BroadcastReceiver()
//    {
//        // действия при получении сообщений
//        public void onReceive(Context context, Intent intent) {
//            int task = intent . getIntExtra (PARAM_TASK, 0);
//            int status = intent . getIntExtra (PARAM_STATUS, 0);
//            Log.d(LOG_TAG, "onReceive: task = " + task + ", status = " + status);
//
//            // Ловим сообщения о старте задач
//            if (status == STATUS_START) {
//                switch(task) {
//                    case TASK1_CODE :
//                    tvTask1.setText("Task1 start");
//                    break;
//                    case TASK2_CODE :
//                    tvTask2.setText("Task2 start");
//                    break;
//                    case TASK3_CODE :
//                    tvTask3.setText("Task3 start");
//                    break;
//                }
//            }
//
//            // Ловим сообщения об окончании задач
//            if (status == STATUS_FINISH) {
//                int result = intent . getIntExtra (PARAM_RESULT, 0);
//                switch(task) {
//                    case TASK1_CODE :
//                    tvTask1.setText("Task1 finish, result = " + result);
//                    break;
//                    case TASK2_CODE :
//                    tvTask2.setText("Task2 finish, result = " + result);
//                    break;
//                    case TASK3_CODE :
//                    tvTask3.setText("Task3 finish, result = " + result);
//                    break;
//                }
//            }
//        }
//    };
}