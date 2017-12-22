package ru.crew.motley.piideo.network;

import com.google.gson.Gson;

import org.parceler.Parcel;

/**
 * Created by vas on 12/18/17.
 */
@Parcel(Parcel.Serialization.BEAN)
public class Member {

    private String id;
    private String phoneNumber;
    private boolean registered;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public static Member fromJson(String jsonString) {
        return new Gson().fromJson(jsonString, Member.class);
    }
}
