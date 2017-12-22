package ru.crew.motley.piideo.network.neo.transaction;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vas on 12/18/17.
 */

public class RowMeta {

    @SerializedName("id")
    private int id;
    @SerializedName("type")
    private String type;
    @SerializedName("deleted")
    private boolean deleted;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
