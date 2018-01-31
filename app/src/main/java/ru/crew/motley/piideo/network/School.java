package ru.crew.motley.piideo.network;

import com.google.gson.Gson;

import org.parceler.Parcel;

/**
 * Created by vas on 1/3/18.
 */
@Parcel(Parcel.Serialization.BEAN)
public class School {
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static School fromJson(String jsonString){
        return new Gson().fromJson(jsonString, School.class);
    }
}
