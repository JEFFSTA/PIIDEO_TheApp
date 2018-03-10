package ru.crew.motley.piideo.network;

import com.google.gson.Gson;

import org.parceler.Parcel;

@Parcel(Parcel.Serialization.BEAN)
public class Member {

    private Long id;
    private String phoneNumber;
    private String countryCode;
    private String phonePrefix;
    private String chatId;
    private boolean registered;
    private Subject subject;
    private School mSchool;
    private Member mReceivedFrom;

    private int flagResId;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public School getSchool() {
        return mSchool;
    }

    public void setSchool(School school) {
        mSchool = school;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public Member getReceivedFrom() {
        return mReceivedFrom;
    }

    public void setReceivedFrom(Member receivedFrom) {
        mReceivedFrom = receivedFrom;
    }

    public int getFlagResId() {
        return flagResId;
    }

    public void setFlagResId(int flagResId) {
        this.flagResId = flagResId;
    }

    public static Member fromJson(String jsonString) {
        return new Gson().fromJson(jsonString, Member.class);
    }

    @Override
    public String toString() {
        return "Member { " +
                " phoneNumber=\'" + phoneNumber + "\'" +
                ", chatId=\'" + chatId + "\'" +
                ", registered=" + registered +
                '}';
    }
}
