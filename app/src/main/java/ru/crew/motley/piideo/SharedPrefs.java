package ru.crew.motley.piideo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by vas on 12/19/17.
 */

public class SharedPrefs {

    private static final String PREFS = "prefs";

    private static final String REGISTERED = "registered";
    private static final String MEMBER_SUBJECT = "memberSubject";
    private static final String SEARCH_SUBJECT = "searchSubject";
    private static final String PHONE = "memberPhone";

    private static final String PAGE = "page";
    private static final String PAGE_START_TIME = "startTime";
    private static final String PAGE_MESSAGE_ID = "receiverId";

    private static final String VERIFICATION_ID = "verificationId";


    public static void searchSubject(String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(SEARCH_SUBJECT, value)
                .apply();
    }

    public static String getSearchSubject(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(SEARCH_SUBJECT, null);
    }

    public static void verificationId(String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(VERIFICATION_ID, value)
                .apply();
    }

    public static String getVerificationId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(VERIFICATION_ID, null);
    }

    public static void savePageStartTime(long timeInMillis, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(PAGE_START_TIME, timeInMillis)
                .apply();
    }

    public static void savePageMessageId(String dbMessageId, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PAGE_MESSAGE_ID, dbMessageId)
                .apply();
    }

    public static long loadPageStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long result = prefs.getLong(PAGE_START_TIME, -1);
        return result;
    }

    public static String loadPageMessageId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String result = prefs.getString(PAGE_MESSAGE_ID, null);
        return result;
    }

    public static void clearPageStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(PAGE_START_TIME)
                .apply();
    }

    public static void clearPageMessageId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(PAGE_MESSAGE_ID)
                .apply();
    }

    public static void clearPageData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(PAGE_MESSAGE_ID)
                .remove(PAGE_START_TIME)
                .remove(PAGE)
                .apply();
    }

}
