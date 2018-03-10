package ru.crew.motley.piideo.search;

import android.content.Context;

/**
 * Created by vas on 3/10/18.
 */

public class Country {

    private String mCountryCodeStr;
    private String mFileName;

    public Country(String str, int num) {
        String[] data = str.split(",");
        mCountryCodeStr = data[2];
        mFileName = String.format("f%03d", num);
    }

    public String getFileName() {
        return mFileName;
    }

    public String getCountryCodeStr() {
        return mCountryCodeStr;
    }
}
