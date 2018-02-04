package ru.crew.motley.piideo.piideo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Recorder extends Service {
    private static final String TAG = Recorder.class.getSimpleName();
    private static final String EXTRA_PIIDEO_NAME = "extraPiideoName";
    private static final String HOMER_DIR = "Piideo";
    public static final String HOME_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/" +
            HOMER_DIR + "/";


    private static final SimpleDateFormat mTimeFormat =
            new SimpleDateFormat("mm:ss", Locale.getDefault());

    private MediaRecorder mRecorder;

    private String recName;
    private String recPath;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;

    private OnTimerChangedListener mOnTimerChangedListener;
    private Timer mTimer;
    private TimerTask mIncrementTimerTask;

    interface OnTimerChangedListener {
        void onTimerChanged(int seconds);
    }

    public static Intent getIntent(Context context, String name) {
        Intent intent = new Intent(context, Recorder.class);
        intent.putExtra(EXTRA_PIIDEO_NAME, name);
        return intent;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String name = intent.getStringExtra(EXTRA_PIIDEO_NAME);
        try {
            setFileNamesAndPath(name);
        } catch (IOException ex) {

        }
        try {
            startRecording();
        } catch (IOException ex) {

        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException ex) {
                Log.d("Recorder", " Catch while sleep");
            }
            stopRecording();
        }
        super.onDestroy();
    }

    private void startRecording() throws IOException {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioEncodingBitRate(192000);
        mRecorder.setOutputFile(recPath);
//        mRecorder.
        mRecorder.prepare();
        mRecorder.start();
        Log.d("Recorder", "started");
        mStartingTimeMillis = System.currentTimeMillis();

    }

    private void setFileNamesAndPath(String name) throws IOException {
        recName = name + ".mp4";
        recPath = HOME_PATH + recName;
        File homeDir = new File(HOME_PATH);
        if (!homeDir.exists()) {
            homeDir.mkdir();
        }
        File file = new File(recPath);
        if (file.exists()) {
            file.createNewFile();
        }
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mElapsedMillis = System.currentTimeMillis() - mStartingTimeMillis;
            mRecorder.release();
            mRecorder = null;
        }
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mOnTimerChangedListener.onTimerChanged(++mElapsedSeconds);
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mgr.notify(1, createNotification());
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    private Notification createNotification() {
        throw new UnsupportedOperationException();
    }
}
