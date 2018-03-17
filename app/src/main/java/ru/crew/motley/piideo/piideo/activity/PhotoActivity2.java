package ru.crew.motley.piideo.piideo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import ru.crew.motley.piideo.R;
import ru.crew.motley.piideo.piideo.fragment.PhotoFragment;

public class PhotoActivity2 extends AppCompatActivity {

    private static final String EXTRA_MESSAGE = "fcm_message";
    private static final String EXTRA_MESSAGE_ID = "local_db_id";

    public static Intent getIntent(String dbMessageId, Parcelable message, Context context) {
        Intent i =  new Intent(context, PhotoActivity2.class);
        i.putExtra(EXTRA_MESSAGE, message);
        i.putExtra(EXTRA_MESSAGE_ID, dbMessageId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo2);
        Parcelable message = getIntent().getParcelableExtra(EXTRA_MESSAGE);
        String messageId = getIntent().getStringExtra(EXTRA_MESSAGE_ID);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PhotoFragment.newInstance(message, messageId))
                    .commit();
        }
    }

}
