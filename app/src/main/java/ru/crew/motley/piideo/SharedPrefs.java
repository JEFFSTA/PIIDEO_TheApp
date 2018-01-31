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

    private static final String VERIFICATION_ID = "verificationId";

    public static void register(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(REGISTERED, true)
                .apply();
    }

    public static boolean isRegistered(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getBoolean(REGISTERED, false);
    }

    public static void memberSubject(String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(MEMBER_SUBJECT, value)
                .apply();
    }

    public static String getMemberSubject(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(MEMBER_SUBJECT, null);
    }

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

    public static void memberPhone(String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(PHONE, value)
                .apply();
    }

    public static String getMemberPhone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return prefs.getString(PHONE, null);
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

}
