package ru.crew.motley.piideo;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

    private static final String PREFS = "prefs";

    private static final String SEARCH_SUBJECT = "searchSubject";
    private static final String REQUEST_MESSAGE = "requestMessage";
    private static final String SEARCHING = "searching";
    private static final String SEARCHING_COUNT = "searching_count";
    private static final String SEARCH_START_TIME = "searching_start_time";

    private static final String CHAT = "page";
    private static final String CHAT_START_TIME = "chatStartTime";
    private static final String CHAT_MESSAGE_ID = "chatReceiverId";

    private static final String HANDSHAKE_START_TIME = "handshakeStartTime";

    private static final String VERIFICATION_ID = "verificationId";

    private static final String PROGRESS_TIME = "startSearchingTime";


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

    public static void requestMessage(String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(REQUEST_MESSAGE, value)
                .apply();
    }

    public static String getRequestMessage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(REQUEST_MESSAGE, null);
    }

    public static void clearRequestMessage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(REQUEST_MESSAGE)
                .apply();
    }

    public static void setSearching(boolean value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(SEARCHING, value)
                .apply();
    }

    public static void setSearchCount(int value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(SEARCHING_COUNT, value)
                .apply();
    }

    public static int getSearchCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getInt(SEARCHING_COUNT, -1);
    }

    public static boolean isSearching(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(SEARCHING, false);
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

    public static void saveChatStartTime(long timeInMillis, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(CHAT_START_TIME, timeInMillis)
                .apply();
    }

    public static void saveChatMessageId(String dbMessageId, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(CHAT_MESSAGE_ID, dbMessageId)
                .apply();
    }

    public static long loadChatStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long result = prefs.getLong(CHAT_START_TIME, -1);
        return result;
    }

    public static String loadChatMessageId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String result = prefs.getString(CHAT_MESSAGE_ID, null);
        return result;
    }

    public static void clearChatStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(CHAT_START_TIME)
                .apply();
    }

    public static void clearChatMessageId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(CHAT_MESSAGE_ID)
                .apply();
    }

    public static void clearChatData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(CHAT_MESSAGE_ID)
                .remove(CHAT_START_TIME)
                .remove(CHAT)
                .apply();
    }


    public static void saveHandshakeStartTime(long timeInMillis, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(HANDSHAKE_START_TIME, timeInMillis)
                .apply();
    }


    public static long loadHandshakeStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long result = prefs.getLong(HANDSHAKE_START_TIME, -1);
        return result;
    }

    public static void clearHandshakeStartTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(HANDSHAKE_START_TIME)
                .apply();
    }

    public static void startSearchingTime(long millis, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(SEARCH_START_TIME, millis)
                .apply();
    }

    public static long loadStartSearchingTime(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long result = prefs.getLong(SEARCH_START_TIME, -1);
        return result;
    }

    public static boolean searchCompleted(Context context) {
        long endTime = SharedPrefs.getSearchCount(context) * 50 * 1000 + SharedPrefs.loadStartSearchingTime(context);
        return endTime < System.currentTimeMillis();
    }
}
