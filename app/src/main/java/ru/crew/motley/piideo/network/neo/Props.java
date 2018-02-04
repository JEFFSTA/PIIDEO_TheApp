package ru.crew.motley.piideo.network.neo;

import com.google.gson.annotations.SerializedName;

public class Props {
    @SerializedName("name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
